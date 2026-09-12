package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<Long> merchantThreadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    public static void setCurrentMerchantId(Long id) {
        merchantThreadLocal.set(id);
    }

    public static Long getCurrentMerchantId() {
        return merchantThreadLocal.get();
    }

    public static void removeCurrentMerchantId() {
        merchantThreadLocal.remove();
    }

}
