package com.sky.filter;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class UserJwtTokenFilter extends OncePerRequestFilter {
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/user/auth/**",
            "/user/dish/**",
            "/doc.html",
            "/webjars/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/user/") || isExcluded(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return;
        }

        try {
            Claims claims = JwtUtil.parseJWT(token);
            Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
            if (userIdObj == null) {
                response.setStatus(401);
                return;
            }
            Long userId = Long.valueOf(userIdObj.toString());
            BaseContext.setCurrentId(userId);
            log.info("当前用户id: {}", userId);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            response.setStatus(401);
        } finally {
            BaseContext.removeCurrentId();
        }
    }

    private boolean isExcluded(String uri) {
        for (String path : EXCLUDE_PATHS) {
            if (pathMatcher.match(path, uri)) {
                return true;
            }
        }
        return false;
    }
}
