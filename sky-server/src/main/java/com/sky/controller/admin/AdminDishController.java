package com.sky.controller.admin;

import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "管理员端菜品接口")
public class AdminDishController {

    @Autowired
    private DishService dishService;

    /**
     * 管理员修改菜品
     * 自动执行：先更新数据库 → 再删除缓存（保证一致）
     */
    @PutMapping("/update")
    @ApiOperation("修改菜品")
    public Result<String> update(@RequestBody Dish dish) {
        dishService.updateById(dish);
        return Result.success("菜品修改成功");
    }
}