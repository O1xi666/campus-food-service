package com.sky.exception;

/**
 * 限流异常：请求超过滑动窗口阈值时抛出，由全局异常处理器统一转成友好的 Result。
 */
public class RateLimitException extends BaseException {

    public RateLimitException(String msg) {
        super(msg);
    }
}