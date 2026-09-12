package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.result.Result;
import com.sky.service.CUserAuthService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/auth")
@Api(tags = "C端用户认证接口")
@Slf4j
public class UserAuthController {
    @Autowired
    private CUserAuthService cUserAuthService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册: {}", userRegisterDTO.getUsername());
        cUserAuthService.register(userRegisterDTO);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录: {}", userLoginDTO.getUsername());
        return Result.success(cUserAuthService.login(userLoginDTO));
    }
}
