package com.sky.controller.user;

import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/user/dish")
@Api(tags = "用户端菜品接口")
public class UserDishController {

    @Autowired
    private DishService dishService;

    /**
     * 用户查询菜品 → 走二级缓存（本地+Redis）
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<Dish> getById(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    @GetMapping("/list")
    @ApiOperation("查询菜品列表")
    public Result<List<Dish>> list() {
        return Result.success(dishService.listForUser());
    }
}