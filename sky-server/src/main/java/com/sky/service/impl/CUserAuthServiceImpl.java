package com.sky.service.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.entity.User;
import com.sky.exception.BusinessException;
import com.sky.mapper.CUserAuthMapper;
import com.sky.service.CUserAuthService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CUserAuthServiceImpl implements CUserAuthService {

    @Autowired
    private CUserAuthMapper cUserAuthMapper;

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        validateUserCredential(userRegisterDTO.getUsername(), userRegisterDTO.getPassword());
        User dbUser = cUserAuthMapper.getByUsername(userRegisterDTO.getUsername());
        if (dbUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = User.builder()
                .name(userRegisterDTO.getUsername())
                .password(md5(userRegisterDTO.getPassword()))
                .createTime(LocalDateTime.now())
                .build();
        cUserAuthMapper.insert(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        validateUserCredential(userLoginDTO.getUsername(), userLoginDTO.getPassword());
        User user = cUserAuthMapper.getByUsernameAndPassword(userLoginDTO.getUsername(), md5(userLoginDTO.getPassword()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getName());
        String token = JwtUtil.createJWT(claims);
        return UserLoginVO.builder()
                .id(user.getId())
                .username(user.getName())
                .token(token)
                .build();
    }

    private void validateUserCredential(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("用户名和密码不能为空");
        }
    }

    private String md5(String source) {
        return DigestUtils.md5DigestAsHex(source.getBytes(StandardCharsets.UTF_8));
    }
}
