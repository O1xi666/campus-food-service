package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import java.util.List;

public interface ShoppingCartService {
    void add(ShoppingCartDTO shoppingCartDTO);

    void sub(ShoppingCartDTO shoppingCartDTO);

    void updateNumber(Long id, Integer number);

    List<ShoppingCart> list();
}
