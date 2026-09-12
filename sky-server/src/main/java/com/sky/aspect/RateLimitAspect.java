package com.sky.aspect;

import com.sky.annotation.RateLimit;
import com.sky.context.BaseContext;
import com.sky.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 限流切面：基于 Redis ZSET 的滑动窗口。
 *
 * 相比固定窗口（每 60s 清零）不会出现"窗口边界双倍流量"的问题：
 * 每次请求先把 [now - window, now] 之外的成员删掉，再看窗口内还剩多少，
 * 整个"清理 + 计数 + 写入"在 Lua 里原子完成，并发下不会超卖。
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private static final String KEY_PREFIX = "rate:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final DefaultRedisScript<Long> slidingWindowScript = new DefaultRedisScript<>();

    public RateLimitAspect() {
        slidingWindowScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/sliding_window.lua")));
        slidingWindowScript.setResultType(Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String base = rateLimit.key().isEmpty() ? defaultKey(joinPoint) : rateLimit.key();
        String member = System.nanoTime() + "-" + ThreadLocalRandom.current().nextInt(1_000_000);

        for (RateLimit.Dimension dimension : rateLimit.dimensions()) {
            String key = buildKey(base, dimension);
            if (!tryAcquire(key, rateLimit.window(), rateLimit.limit(), member)) {
                log.warn("触发限流 - 维度: {}, key: {}, 阈值: {}/{}s", dimension, key, rateLimit.limit(), rateLimit.window());
                throw new RateLimitException("请求过于频繁，请稍后再试");
            }
        }
        return joinPoint.proceed();
    }

    private boolean tryAcquire(String key, long windowSeconds, long limit, String member) {
        Long result = redisTemplate.execute(slidingWindowScript,
                Collections.singletonList(key),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(windowSeconds * 1000L),
                String.valueOf(limit),
                member);
        return result != null && result == 1L;
    }

    private String buildKey(String base, RateLimit.Dimension dimension) {
        switch (dimension) {
            case GLOBAL:
                return KEY_PREFIX + "global:" + base;
            case USER:
                Long userId = BaseContext.getCurrentId();
                return userId != null
                        ? KEY_PREFIX + "user:" + userId + ":" + base
                        : KEY_PREFIX + "ip:" + clientIp() + ":" + base;
            case IP:
            default:
                return KEY_PREFIX + "ip:" + clientIp() + ":" + base;
        }
    }

    private String defaultKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    /** 依次取 X-Forwarded-For / X-Real-IP / remoteAddr，兼容经过 Nginx 反向代理的场景 */
    private String clientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}