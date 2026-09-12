package com.sky.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解：基于 Redis ZSET 的滑动窗口计数。
 *
 * 支持 全局 / IP / 用户 三个维度，可同时生效，任一维度超限即拒绝。
 * 例：@RateLimit(key = "ai:recommend", limit = 20, window = 60,
 *                dimensions = {GLOBAL, IP, USER})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 业务标识，默认取"类名.方法名" */
    String key() default "";

    /** 窗口内允许的最大请求数 */
    long limit() default 20;

    /** 窗口大小（秒） */
    long window() default 60;

    /** 生效维度，可多选 */
    Dimension[] dimensions() default {Dimension.IP};

    enum Dimension {
        /** 全站总量 */
        GLOBAL,
        /** 单个 IP */
        IP,
        /** 单个用户（未登录时退化为 IP） */
        USER
    }
}