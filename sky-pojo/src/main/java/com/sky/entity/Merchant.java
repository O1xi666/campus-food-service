package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String phone;

    private String address;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
