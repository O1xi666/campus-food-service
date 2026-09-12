package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsSalesDTO implements Serializable {
    //鍟嗗搧鍚嶇О
    private String name;

    //閿€閲?
    private Integer number;

    //菜品ID
    private Long dishId;

    //商家ID
    private Long merchantId;
}


