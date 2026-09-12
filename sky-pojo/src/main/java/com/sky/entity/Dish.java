package com.sky.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鑿滃搧
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dish")
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //鑿滃搧鍚嶇О
    private String name;

    //鑿滃搧鍒嗙被id
    private Long categoryId;

    //鑿滃搧浠锋牸
    private BigDecimal price;

    //鍥剧墖
    private String image;

    //鎻忚堪淇℃伅
    private String description;

    //0 鍋滃敭 1 璧峰敭
    private Integer status;

    //搴撳瓨
    private Integer stock;
    
    //所属商家ID
    private Long merchantId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

}
