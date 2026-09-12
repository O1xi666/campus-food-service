package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
@Slf4j
public class Ordercontroller {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO 下单数据
     * @return 下单结果
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 用户端订单分页查询
     * @param ordersPageQueryDTO 分页与查询条件
     * @return 分页订单数据
     */
    @GetMapping("/page")
    @ApiOperation("用户订单分页查询")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("用户订单分页查询: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery4User(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 用户端订单详情
     * @param ordersDTO 订单id
     * @return 订单详情
     */
   @GetMapping("/detail")
@ApiOperation("用户订单详情")
public Result<OrderVO> detail(@RequestParam Long id) { // 直接用Long id接收，和Service匹配
    log.info("查询订单详情: {}", id);
    OrderVO orderVO = orderService.orderDetail(id); // 直接传id
    return Result.success(orderVO);
    }

    /**
     * 购物车批量下单
     */
    @PostMapping("/submit/cart")
    @ApiOperation("购物车批量下单")
    public Result<OrderSubmitVO> submitCart(@RequestBody OrdersSubmitDTO ordersSubmitDTO,
                                            @RequestParam List<Long> cartIds) {
        log.info("购物车批量下单，cartIds: {}, body: {}", cartIds, ordersSubmitDTO);
        return Result.success(orderService.submitCartOrder(ordersSubmitDTO, cartIds));
    }
}
