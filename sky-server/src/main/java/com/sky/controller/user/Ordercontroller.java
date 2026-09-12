package com.sky.controller.user;

import com.sky.annotation.RateLimit;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.SeckillDTO;
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
    // 秒杀不做全局限流：削峰交给 Redis 库存预扣减，这里只防单用户 / 单 IP 刷接口
    @RateLimit(key = "order:submit", limit = 10, window = 1,
            dimensions = {RateLimit.Dimension.USER, RateLimit.Dimension.IP})
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
    @RateLimit(key = "order:page", limit = 60, window = 60,
            dimensions = {RateLimit.Dimension.USER, RateLimit.Dimension.IP})
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("用户订单分页查询: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery4User(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 用户端订单详情
     * @param id 订单id
     * @return 订单详情
     */
    @GetMapping("/detail")
    @ApiOperation("用户订单详情")
    public Result<OrderVO> detail(@RequestParam Long id) {
        log.info("查询订单详情: {}", id);
        OrderVO orderVO = orderService.orderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 购物车批量下单（秒杀链路：Redis+Lua 预扣减 → MQ 异步落库）
     */
    @PostMapping("/submit/cart")
    @ApiOperation("购物车批量下单")
    // 秒杀不做全局限流：削峰交给 Redis 库存预扣减，这里只防单用户 / 单 IP 刷接口
    @RateLimit(key = "order:submit", limit = 10, window = 1,
            dimensions = {RateLimit.Dimension.USER, RateLimit.Dimension.IP})
    public Result<OrderSubmitVO> submitCart(@RequestBody OrdersSubmitDTO ordersSubmitDTO,
                                            @RequestParam List<Long> cartIds) {
        log.info("购物车批量下单，cartIds: {}, body: {}", cartIds, ordersSubmitDTO);
        return Result.success(orderService.submitCartOrder(ordersSubmitDTO, cartIds));
    }

    /**
     * 秒杀专用下单入口：请求体直接携带菜品与数量，不读 DB 购物车，
     * 使 Redis 资格校验成为请求路径上的第一个环节。
     */
    @PostMapping("/seckill")
    @ApiOperation("秒杀下单（专用入口）")
    @RateLimit(key = "order:seckill", limit = 100000, window = 1,
            dimensions = {RateLimit.Dimension.USER, RateLimit.Dimension.IP})
    public Result<OrderSubmitVO> seckill(@RequestBody SeckillDTO seckillDTO) {
        log.info("秒杀下单: {}", seckillDTO);
        return Result.success(orderService.seckillOrder(seckillDTO));
    }
}