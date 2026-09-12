package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sky.dto.GoodsSalesDTO;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    BigDecimal sumCompletedAmountByTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    Integer countCompletedOrdersByTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 查询已完成订单的总销售额
     */
    BigDecimal sumTotalSales();
    BigDecimal sumTotalSales(@Param("merchantId") Long merchantId);

    /**
     * 查询已完成订单的总数
     */
    Integer countTotalOrders();
    Integer countTotalOrders(@Param("merchantId") Long merchantId);

    /**
     * 查询销量 TOP5 的菜品
     */
    List<GoodsSalesDTO> selectSalesTop5();
    List<GoodsSalesDTO> selectSalesTop5(@Param("merchantId") Long merchantId);

    /**
     * 查询销量最差的 5 个菜品
     */
    List<GoodsSalesDTO> selectSalesBottom5();
    List<GoodsSalesDTO> selectSalesBottom5(@Param("merchantId") Long merchantId);

    /**
     * 查询所有菜品的销量排行（用于初始化 Redis zset）
     */
   @Select("SELECT od.dish_id as dishId, od.name, SUM(od.number) as number "
            + ", o.merchant_id as merchantId "
           + "FROM order_detail od JOIN orders o ON od.order_id = o.id "
           + "WHERE o.status = 5 AND o.pay_status = 1 "
            + "AND o.merchant_id IS NOT NULL "
           + "GROUP BY od.dish_id, od.name, o.merchant_id ORDER BY number DESC")
    List<GoodsSalesDTO> selectAllSalesRank();
}
