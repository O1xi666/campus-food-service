package com.sky.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * MQ 消息体 —— 封装秒杀异步落库所需的订单数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFlashMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNumber;
    private Long userId;
    private Long addressBookId;
    private BigDecimal amount;
    private String remark;
    private String phone;
    private String consignee;
    private String address;
    private String userName;
    private Long merchantId;

    /** 订单明细 */
    private List<OrderDetailItem> orderDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long dishId;
        private String name;
        private String dishFlavor;
        private Integer number;
        private BigDecimal amount;
        private String image;
        private Long setmealId;
    }
}
