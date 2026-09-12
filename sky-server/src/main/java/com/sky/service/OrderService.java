package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderVO;
import com.sky.vo.OrderSubmitVO;
import java.util.List;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 用户端：查询我的订单列表（分页）
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 用户端：查询订单详情
     * @param id
     * @return
     */
    OrderVO orderDetail(Long id);

    /**
     * 购物车批量下单
     * @param ordersSubmitDTO 订单基础信息
     * @param cartIds 购物车id列表
     * @return 下单结果
     */
    OrderSubmitVO submitCartOrder(OrdersSubmitDTO ordersSubmitDTO, List<Long> cartIds);

    /**
     * 管理端：订单分页条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 管理端：修改订单状态
     * @param ordersDTO
     */
    void updateStatus(OrdersDTO ordersDTO);
}
