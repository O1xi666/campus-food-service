package com.sky.dto;

import lombok.Data;

/**
 * 员工新增时传递的数据模型
 */
@Data // 加这个注解，不用手写get/set方法
public class EmployeeDTO {
    private long id;
    private String username; // 用户名
    private String name; // 姓名
    private String phone; // 手机号
    private String sex; // 性别
    private String idNumber; // 身份证号
}