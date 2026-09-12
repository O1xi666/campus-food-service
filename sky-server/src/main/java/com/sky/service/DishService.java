package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.Dish;
import java.util.List;

public interface DishService extends IService<Dish> {
    /**
     * 用户端查询可浏览菜品
     * @return 菜品列表
     */
    List<Dish> listForUser();
}
