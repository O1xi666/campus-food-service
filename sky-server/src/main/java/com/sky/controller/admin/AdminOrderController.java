package com.sky.controller.admin;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "管理员端订单接口")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 管理端订单分页条件查询
     */
    @GetMapping("/page")
    @ApiOperation("订单分页条件查询")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        return Result.success(orderService.conditionSearch(ordersPageQueryDTO));
    }

    /**
     * 管理端修改订单状态
     */
    @PutMapping("/status")
    @ApiOperation("修改订单状态")
    public Result<String> updateStatus(@RequestBody OrdersDTO ordersDTO) {
        orderService.updateStatus(ordersDTO);
        return Result.success("订单状态修改成功");
    }
}
