package com.sky.mapper;

import com.sky.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantMapper {

    @Select("select * from merchant where id = #{id}")
    Merchant getById(Long id);
}
