package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CUserAuthMapper {

    // 1. 根据用户名查询（注册校验）
    @Select("select * from user where name = #{username}")
    User getByUsername(String username);

    // 2. 插入用户（注册）
    @Insert("insert into user (name, password, create_time) values (#{name}, #{password}, #{createTime})")
    void insert(User user);

    // 3. 【关键修复！】登录查询：加上@Select注解，查name和password字段
    @Select("select * from user where name = #{username} and password = #{password}")
    User getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}