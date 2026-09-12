package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀下单请求：直接携带菜品与数量，不依赖 DB 购物车，
 * 使 Redis 资格校验成为请求路径上的第一个环节。
 */
@Data
public class SeckillDTO implements Serializable {

    private Long dishId;
    private Integer number;
    private Long addressBookId;
    private String remark;
}
