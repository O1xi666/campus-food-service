package com.sky.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderConsumer {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "order.flash.queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleFlashOrder(OrderFlashMessage msg) {
        log.info("异步落库开始 - 订单号: {}", msg.getOrderNumber());

        // 1. 构建 Orders
        Orders orders = Orders.builder()
                .number(msg.getOrderNumber())
                .status(Orders.TO_BE_CONFIRMED)
                .userId(msg.getUserId())
                .addressBookId(msg.getAddressBookId())
                .orderTime(LocalDateTime.now())
                .checkoutTime(LocalDateTime.now())
                .payStatus(Orders.PAID)
                .amount(msg.getAmount())
                .remark(msg.getRemark())
                .phone(msg.getPhone())
                .consignee(msg.getConsignee())
                .address(msg.getAddress())
                .userName(msg.getUserName())
                .merchantId(msg.getMerchantId())
                .build();
        orderMapper.insert(orders);

        // 2. 构建 OrderDetail 列表
        List<OrderDetail> details = msg.getOrderDetails().stream()
                .map(item -> OrderDetail.builder()
                        .orderId(orders.getId())
                        .dishId(item.getDishId())
                        .name(item.getName())
                        .dishFlavor(item.getDishFlavor())
                        .number(item.getNumber())
                        .amount(item.getAmount())
                        .image(item.getImage())
                        .setmealId(item.getSetmealId())
                        .build())
                .collect(Collectors.toList());
        orderDetailMapper.insertBatch(details);

        // 3. 扣减 MySQL 库存（与 Redis 预扣减保持一致）
        for (OrderFlashMessage.OrderDetailItem item : msg.getOrderDetails()) {
            dishMapper.update(null,
                    Wrappers.<Dish>lambdaUpdate()
                            .setSql("stock = stock - " + item.getNumber())
                            .eq(Dish::getId, item.getDishId())
                            .ge(Dish::getStock, item.getNumber()));
        }

        // 4. 更新 Redis 菜品销量排行榜（实时 Top N 数据源）
        for (OrderFlashMessage.OrderDetailItem item : msg.getOrderDetails()) {
            redisTemplate.opsForZSet().incrementScore(
                    "dish:sales:rank:" + msg.getMerchantId(),
                    item.getDishId().toString(),
                    item.getNumber().doubleValue()
            );
        }

        log.info("异步落库完成 - 订单号: {}, 订单ID: {}", msg.getOrderNumber(), orders.getId());
    }
}

