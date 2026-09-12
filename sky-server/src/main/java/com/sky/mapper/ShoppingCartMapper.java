package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    ShoppingCart getByUserAndDish(@Param("userId") Long userId,
                                  @Param("dishId") Long dishId,
                                  @Param("dishFlavor") String dishFlavor);

    void insert(ShoppingCart shoppingCart);

    void updateNumberById(@Param("id") Long id, @Param("number") Integer number);

    List<ShoppingCart> listByUserId(Long userId);

    List<ShoppingCart> listByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    void deleteById(Long id);

    void deleteBatchByIds(@Param("ids") List<Long> ids);
}
