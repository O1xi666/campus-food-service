package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("token");

        if (token == null || token.trim().isEmpty()) {
            log.info("令牌为空，未登录");
            response.setStatus(401);
            return false;
        }

        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());

            // 解析 merchantId
            Object merchantIdObj = claims.get(JwtClaimsConstant.MERCHANT_ID);
            if (merchantIdObj != null) {
                Long merchantId = Long.valueOf(merchantIdObj.toString());
                BaseContext.setCurrentMerchantId(merchantId);
            }

            // 存储当前用户ID
            BaseContext.setCurrentId(empId);

            log.info("当前员工id：{}", empId);
            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
        BaseContext.removeCurrentMerchantId();
    }
}
