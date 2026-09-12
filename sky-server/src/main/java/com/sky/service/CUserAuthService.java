package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.vo.UserLoginVO;

public interface CUserAuthService {
    void register(UserRegisterDTO userRegisterDTO);

    UserLoginVO login(UserLoginDTO userLoginDTO);
}
