package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    /**
     * 查询菜品 → 走二级缓存(本地+Redis)
     */
    @Cacheable(value = "dishCache", key = "#id")
    @Override
    public Dish getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    @Cacheable(value = "dishCache", key = "'user:list'")
    public List<Dish> listForUser() {
        return lambdaQuery()
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getUpdateTime)
                .list();
    }

    /**
     * 修改菜品 → 先更库 → 自动删缓存
     */
    @CacheEvict(value = "dishCache", allEntries = true)
    @Override
    public boolean updateById(Dish dish) {
        return super.updateById(dish);
    }
}
