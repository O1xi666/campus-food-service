package com.sky.dto;

import lombok.Data;

/**
 * 员工登录时传递的数据模型
 */
@Data
public class EmployeeLoginDTO {
    // 登录只需要用户名、密码，别多写！
    private String username;
    private String password;
}
