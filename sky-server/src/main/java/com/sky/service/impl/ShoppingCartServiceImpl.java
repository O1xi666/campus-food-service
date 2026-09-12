package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
        if (dish == null) {
            throw new BusinessException("菜品不存在，无法加入购物车");
        }

        ShoppingCart dbCart = shoppingCartMapper.getByUserAndDish(userId, shoppingCartDTO.getDishId(), shoppingCartDTO.getDishFlavor());
        if (dbCart != null) {
            shoppingCartMapper.updateNumberById(dbCart.getId(), dbCart.getNumber() + 1);
            return;
        }

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(dish.getId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .name(dish.getName())
                .amount(dish.getPrice())
                .image(dish.getImage())
                .number(1)
                .createTime(LocalDateTime.now())
                .build();
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    @Transactional
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart dbCart = shoppingCartMapper.getByUserAndDish(userId, shoppingCartDTO.getDishId(), shoppingCartDTO.getDishFlavor());
        if (dbCart == null) {
            throw new ShoppingCartBusinessException("购物车中不存在该菜品");
        }

        int nextNum = dbCart.getNumber() - 1;
        if (nextNum <= 0) {
            shoppingCartMapper.deleteById(dbCart.getId());
            return;
        }
        shoppingCartMapper.updateNumberById(dbCart.getId(), nextNum);
    }

    @Override
    public void updateNumber(Long id, Integer number) {
        if (number == null || number <= 0) {
            throw new ShoppingCartBusinessException("购物车数量必须大于0");
        }
        shoppingCartMapper.updateNumberById(id, number);
    }

    @Override
    public List<ShoppingCart> list() {
        return shoppingCartMapper.listByUserId(BaseContext.getCurrentId());
    }
}
