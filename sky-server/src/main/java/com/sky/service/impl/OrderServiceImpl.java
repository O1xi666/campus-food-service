package com.sky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.SeckillDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
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

    /** 秒杀执行模式：redis = Redis 预扣减 + MQ 异步落库；direct-db = 同步直连 MySQL（压测基线） */
    @Value("${sky.seckill.mode:redis}")
    private String seckillMode;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
        // ===== Step 1: Redis 资格校验（热点路径，先于任何 DB 读取）=====
        // 库存由发布流程预热进 Redis；没抢到库存的请求在这里就返回，全程不产生 DB 访问。
        Map<Long, Integer> deductedCache = new HashMap<>();
        Long merchantId = null;
        BigDecimal totalAmount = BigDecimal.ZERO;
        String orderNumber = generateOrderNumber();
        try {
            for (OrderDetail detail : details) {
                String stockKey = STOCK_KEY_PREFIX + detail.getDishId();
                Long luaResult = executeDeduct(stockKey, detail.getNumber());

                // -1 = key 不存在（未预热）：冷启动兜底，本次回源补建 key 后重试一次
                if (luaResult != null && luaResult == -1L) {
                    Dish dish = dishMapper.selectById(detail.getDishId());
                    if (dish == null) {
                        throw new BusinessException("菜品不存在");
                    }
                    if (dish.getStock() == null) {
                        throw new BusinessException("菜品库存字段为空，无法下单");
                    }
                    redisTemplate.opsForValue().setIfAbsent(stockKey, dish.getStock().toString());
                    luaResult = executeDeduct(stockKey, detail.getNumber());
                }

                if (luaResult == null || luaResult < 0) {
                    throw new BusinessException("库存不足，请稍后重试");
                }
                deductedCache.merge(detail.getDishId(), detail.getNumber(), Integer::sum);
            }

            // ===== Step 2: 抢到库存的请求才回源（地址 / 菜品商家）=====
            AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            for (OrderDetail detail : details) {
                Dish dish = dishMapper.selectById(detail.getDishId());
                if (dish == null) {
                    throw new BusinessException("菜品不存在");
                }
                if (merchantId == null) {
                    merchantId = dish.getMerchantId();
                }
            }

            // ===== Step 3: 计算总金额 =====
            for (OrderDetail detail : details) {
                totalAmount = totalAmount.add(detail.getAmount().multiply(new BigDecimal(detail.getNumber())));
            }

            // ===== Step 4: 发送 MQ 消息（消费者异步写入 MySQL）=====
            OrderFlashMessage msg = buildFlashMessage(orderNumber, userId, merchantId,
                    ordersSubmitDTO, addressBook, totalAmount, details);
            try {
                log.info("发送 MQ 消息 - 订单号: {}", orderNumber);
                rabbitTemplate.convertAndSend("order.flash.queue", msg);
            } catch (Exception e) {
                log.error("MQ 发送失败，回滚 Redis 库存", e);
                throw new BusinessException("下单繁忙，请稍后重试");
            }
        } catch (RuntimeException ex) {
            rollbackRedisStock(deductedCache);
            throw ex;
        }

        // ===== Step 5: 立即返回（MySQL 写交由 MQ 消费者异步完成）=====
        return OrderSubmitVO.builder()
                .orderNumber(orderNumber)
                .orderAmount(totalAmount)
                .orderTime(LocalDateTime.now())
                .build();
    }

    /**
     * 秒杀专用入口：Redis 资格校验排在最前，抢到库存的才回源 + 投递 MQ。
     * sky.seckill.mode=direct-db 时切换为同步直连 MySQL 的压测基线。
     */
    @Override
    public OrderSubmitVO seckillOrder(SeckillDTO seckillDTO) {
        Long userId = BaseContext.getCurrentId();
        Integer number = seckillDTO.getNumber() == null ? 1 : seckillDTO.getNumber();
        if (seckillDTO.getDishId() == null || number <= 0) {
            throw new BusinessException("参数不合法");
        }
        if ("direct-db".equalsIgnoreCase(seckillMode)) {
            return submitDirectToDb(seckillDTO, number, userId);
        }

        // ---- 热点路径第一步：Redis + Lua 原子预扣减（不碰 DB）----
        String stockKey = STOCK_KEY_PREFIX + seckillDTO.getDishId();
        Long luaResult = executeDeduct(stockKey, number);
        if (luaResult != null && luaResult == -1L) {
            // key 不存在（未预热）：冷启动兜底，本次回源补建 key 后重试一次
            Dish dish = dishMapper.selectById(seckillDTO.getDishId());
            if (dish == null) {
                throw new BusinessException("菜品不存在");
            }
            if (dish.getStock() == null) {
                throw new BusinessException("菜品库存字段为空，无法下单");
            }
            redisTemplate.opsForValue().setIfAbsent(stockKey, dish.getStock().toString());
            luaResult = executeDeduct(stockKey, number);
        }
        if (luaResult == null || luaResult < 0) {
            throw new BusinessException("库存不足，请稍后重试");
        }

        // ---- 抢到库存的请求才回源 ----
        Map<Long, Integer> deducted = new HashMap<>();
        deducted.put(seckillDTO.getDishId(), number);
        try {
            AddressBook addressBook = addressBookMapper.getById(seckillDTO.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            Dish dish = dishMapper.selectById(seckillDTO.getDishId());
            if (dish == null) {
                throw new BusinessException("菜品不存在");
            }
            BigDecimal amount = dish.getPrice().multiply(new BigDecimal(number));
            OrderDetail detail = OrderDetail.builder()
                    .dishId(dish.getId())
                    .name(dish.getName())
                    .number(number)
                    .amount(dish.getPrice())
                    .image(dish.getImage())
                    .build();
            OrdersSubmitDTO dto = new OrdersSubmitDTO();
            dto.setAddressBookId(seckillDTO.getAddressBookId());
            dto.setRemark(seckillDTO.getRemark());
            String orderNumber = generateOrderNumber();
            OrderFlashMessage msg = buildFlashMessage(orderNumber, userId, dish.getMerchantId(),
                    dto, addressBook, amount, Collections.singletonList(detail));
            log.info("发送 MQ 消息 - 订单号: {}", orderNumber);
            rabbitTemplate.convertAndSend("order.flash.queue", msg);
            return OrderSubmitVO.builder()
                    .orderNumber(orderNumber)
                    .orderAmount(amount)
                    .orderTime(LocalDateTime.now())
                    .build();
        } catch (RuntimeException ex) {
            rollbackRedisStock(deducted);
            throw ex;
        }
    }

    /**
     * 压测基线：同步直连 MySQL —— 不走 Redis 预扣减、不走 MQ。
     * 每个请求都在一个事务里读菜品、条件更新扣库存、写订单与明细，
     * 用于量出"没有缓存削峰"时数据库真实承受的 QPS 与行锁竞争。
     */
    private OrderSubmitVO submitDirectToDb(SeckillDTO seckillDTO, Integer number, Long userId) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> {
            Dish dish = dishMapper.selectById(seckillDTO.getDishId());
            if (dish == null) {
                throw new BusinessException("菜品不存在");
            }
            AddressBook addressBook = addressBookMapper.getById(seckillDTO.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            // 条件更新扣库存：WHERE stock >= n，靠数据库行锁保证不超卖
            int rows = dishMapper.update(null,
                    Wrappers.<Dish>lambdaUpdate()
                            .setSql("stock = stock - " + number)
                            .eq(Dish::getId, dish.getId())
                            .ge(Dish::getStock, number));
            if (rows == 0) {
                throw new BusinessException("库存不足，请稍后重试");
            }
            BigDecimal amount = dish.getPrice().multiply(new BigDecimal(number));
            String orderNumber = generateOrderNumber();
            Orders orders = Orders.builder()
                    .number(orderNumber)
                    .status(Orders.TO_BE_CONFIRMED)
                    .userId(userId)
                    .addressBookId(seckillDTO.getAddressBookId())
                    .orderTime(LocalDateTime.now())
                    .checkoutTime(LocalDateTime.now())
                    .payStatus(Orders.PAID)
                    .amount(amount)
                    .remark(seckillDTO.getRemark())
                    .phone(addressBook.getPhone())
                    .consignee(addressBook.getConsignee())
                    .address(addressBook.getProvinceName()
                            + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                            + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                            + (addressBook.getDetail() == null ? "" : addressBook.getDetail()))
                    .merchantId(dish.getMerchantId())
                    .build();
            orderMapper.insert(orders);
            orderDetailMapper.insertBatch(Collections.singletonList(OrderDetail.builder()
                    .orderId(orders.getId())
                    .dishId(dish.getId())
                    .name(dish.getName())
                    .number(number)
                    .amount(dish.getPrice())
                    .image(dish.getImage())
                    .build()));
            return OrderSubmitVO.builder()
                    .id(orders.getId())
                    .orderNumber(orderNumber)
                    .orderAmount(amount)
                    .orderTime(orders.getOrderTime())
                    .build();
        });
    }

    /** 执行 Lua 库存预扣减：只传扣减数量，key 不存在时脚本返回 -1 */
    private Long executeDeduct(String stockKey, Integer number) {
        return redisTemplate.execute(
                deductStockScript,
                Collections.singletonList(stockKey),
                number.toString()
        );
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
