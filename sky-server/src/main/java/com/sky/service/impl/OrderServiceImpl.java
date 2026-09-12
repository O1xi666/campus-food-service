package com.sky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.BusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mq.OrderFlashMessage;
import com.sky.mq.OrderFlashMessage.OrderDetailItem;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    private static final String STOCK_KEY_PREFIX = "dish:stock:";
    private final DefaultRedisScript<Long> deductStockScript = new DefaultRedisScript<>();

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public OrderServiceImpl() {
        deductStockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deduct_stock.lua")));
        deductStockScript.setResultType(Long.class);
    }

    /**
     * 秒杀下单：Redis+Lua 预扣减 → MQ 异步落库
     */
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> cartList = shoppingCartMapper.listByUserId(userId);
        if (cartList == null || cartList.isEmpty()) {
            throw new BusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        List<Long> cartIds = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            cartIds.add(cart.getId());
            orderDetails.add(OrderDetail.builder()
                    .dishId(cart.getDishId())
                    .name(cart.getName())
                    .dishFlavor(cart.getDishFlavor())
                    .number(cart.getNumber())
                    .amount(cart.getAmount())
                    .image(cart.getImage())
                    .build());
        }
        OrderSubmitVO vo = processFlashOrder(ordersSubmitDTO, orderDetails, userId);
        shoppingCartMapper.deleteBatchByIds(cartIds);
        return vo;
    }

    /**
     * 购物车批量秒杀下单
     */
    @Override
    public OrderSubmitVO submitCartOrder(OrdersSubmitDTO ordersSubmitDTO, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            throw new BusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> cartList = shoppingCartMapper.listByIdsAndUserId(cartIds, userId);
        if (cartList == null || cartList.isEmpty()) {
            throw new BusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            orderDetails.add(OrderDetail.builder()
                    .dishId(cart.getDishId())
                    .name(cart.getName())
                    .dishFlavor(cart.getDishFlavor())
                    .number(cart.getNumber())
                    .amount(cart.getAmount())
                    .image(cart.getImage())
                    .build());
        }
        OrderSubmitVO vo = processFlashOrder(ordersSubmitDTO, orderDetails, userId);
        shoppingCartMapper.deleteBatchByIds(cartIds);
        return vo;
    }

    /**
     * 秒杀下单核心：Redis+Lua 预扣减 → MQ 异步落库
     */
    private OrderSubmitVO processFlashOrder(OrdersSubmitDTO ordersSubmitDTO, List<OrderDetail> details, Long userId) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // Step 1: Lua 原子扣减 Redis 库存
        Map<Long, Integer> deductedCache = new HashMap<>();
        Long merchantId = null;
        try {
            for (OrderDetail detail : details) {
                Dish dish = dishMapper.selectById(detail.getDishId());
                if (dish == null) {
                    throw new BusinessException("菜品不存在");
                }
                Integer stock = dish.getStock();
                if (stock == null) {
                    throw new BusinessException("菜品库存字段为空，无法下单");
                }
                Long luaResult = redisTemplate.execute(
                        deductStockScript,
                        Collections.singletonList(STOCK_KEY_PREFIX + dish.getId()),
                        detail.getNumber().toString(),
                        stock.toString()
                );
                if (luaResult == null || luaResult < 0) {
                    throw new BusinessException("库存不足: " + dish.getName());
                }
                deductedCache.put(dish.getId(),
                        deductedCache.getOrDefault(dish.getId(), 0) + detail.getNumber());
                if (merchantId == null) {
                    merchantId = dish.getMerchantId();
                }
            }
        } catch (RuntimeException ex) {
            rollbackRedisStock(deductedCache);
            throw ex;
        }

        // Step 2: 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDetail detail : details) {
            totalAmount = totalAmount.add(detail.getAmount().multiply(new BigDecimal(detail.getNumber())));
        }

        // Step 3: 发送 MQ 消息（消费者异步写入 MySQL）
        String orderNumber = generateOrderNumber();
        OrderFlashMessage msg = buildFlashMessage(orderNumber, userId, merchantId,
                ordersSubmitDTO, addressBook, totalAmount, details);

        try {
            log.info("发送 MQ 消息 - 订单号: {}", orderNumber);
            rabbitTemplate.convertAndSend("order.flash.queue", msg);
        } catch (Exception e) {
            log.error("MQ 发送失败，回滚 Redis 库存", e);
            rollbackRedisStock(deductedCache);
            throw new BusinessException("下单繁忙，请稍后重试");
        }

        // Step 4: 立即返回（MySQL 写交由 MQ 消费者异步完成）
        return OrderSubmitVO.builder()
                .orderNumber(orderNumber)
                .orderAmount(totalAmount)
                .orderTime(LocalDateTime.now())
                .build();
    }

    /** Redis 预扣减失败时回滚 */
    private void rollbackRedisStock(Map<Long, Integer> deductedCache) {
        for (Map.Entry<Long, Integer> entry : deductedCache.entrySet()) {
            redisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + entry.getKey(), entry.getValue());
        }
    }

    /** 生成唯一订单号 */
    private String generateOrderNumber() {
        int random = new Random().nextInt(9000) + 1000;
        return System.currentTimeMillis() + String.valueOf(random);
    }

    /** 构建 MQ 消息体 */
    private OrderFlashMessage buildFlashMessage(String orderNumber, Long userId, Long merchantId,
                                                 OrdersSubmitDTO dto, AddressBook addressBook,
                                                 BigDecimal totalAmount, List<OrderDetail> details) {
        List<OrderDetailItem> items = details.stream()
                .map(d -> OrderDetailItem.builder()
                        .dishId(d.getDishId())
                        .name(d.getName())
                        .dishFlavor(d.getDishFlavor())
                        .number(d.getNumber())
                        .amount(d.getAmount())
                        .image(d.getImage())
                        .setmealId(d.getSetmealId())
                        .build())
                .collect(Collectors.toList());

        String fullAddress = addressBook.getProvinceName()
                + (addressBook.getCityName() != null ? addressBook.getCityName() : "")
                + (addressBook.getDistrictName() != null ? addressBook.getDistrictName() : "")
                + (addressBook.getDetail() != null ? addressBook.getDetail() : "");

        return OrderFlashMessage.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .addressBookId(dto.getAddressBookId())
                .amount(totalAmount)
                .remark(dto.getRemark())
                .phone(addressBook.getPhone())
                .consignee(addressBook.getConsignee())
                .address(fullAddress)
                .merchantId(merchantId)
                .orderDetails(items)
                .build();
    }

    // ==================== 以下方法保持原有逻辑不变 ====================

    @Override
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        Long currentId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(currentId);

        Page<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        lambdaQuery()
                .eq(Orders::getUserId, ordersPageQueryDTO.getUserId())
                .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                .orderByDesc(Orders::getOrderTime)
                .page(page);

        List<OrderVO> orderVOList = new ArrayList<>();
        if (page.getRecords() != null) {
            for (Orders orders : page.getRecords()) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orders.getId()));
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Long currentId = BaseContext.getCurrentId();
        if (!Objects.equals(orders.getUserId(), currentId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orders.getId()));
        return orderVO;
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        lambdaQuery()
                .like(ordersPageQueryDTO.getNumber() != null && !ordersPageQueryDTO.getNumber().isEmpty(), Orders::getNumber, ordersPageQueryDTO.getNumber())
                .like(ordersPageQueryDTO.getPhone() != null && !ordersPageQueryDTO.getPhone().isEmpty(), Orders::getPhone, ordersPageQueryDTO.getPhone())
                .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                .ge(ordersPageQueryDTO.getBeginTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getBeginTime())
                .le(ordersPageQueryDTO.getEndTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getEndTime())
                .orderByDesc(Orders::getOrderTime)
                .page(page);

        List<OrderVO> orderVOList = new ArrayList<>();
        if (page.getRecords() != null) {
            for (Orders orders : page.getRecords()) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orders.getId()));
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    @Transactional
    public void updateStatus(OrdersDTO ordersDTO) {
        Orders dbOrder = getById(ordersDTO.getId());
        if (dbOrder == null) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer status = ordersDTO.getStatus();
        if (status == null || status < Orders.PENDING_PAYMENT || status > Orders.CANCELLED) {
            throw new BusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (Objects.equals(dbOrder.getStatus(), Orders.COMPLETED) || Objects.equals(dbOrder.getStatus(), Orders.CANCELLED)) {
            throw new BusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        dbOrder.setStatus(status);
        updateById(dbOrder);
    }
}
