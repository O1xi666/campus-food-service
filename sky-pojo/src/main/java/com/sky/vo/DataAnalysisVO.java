package com.sky.vo;

import com.sky.dto.GoodsSalesDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 数据分析结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataAnalysisVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 总销售额
    private BigDecimal totalSales;

    // 总订单数
    private Integer totalOrders;

    // 销量 TOP5 菜品
    private List<GoodsSalesDTO> top5Dishes;

    // 销量最差 5 个菜品
    private List<GoodsSalesDTO> bottom5Dishes;
}
