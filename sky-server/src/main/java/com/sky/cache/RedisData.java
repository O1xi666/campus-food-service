package com.sky.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 逻辑过期包装：缓存数据额外带一个"逻辑过期时间"。
 *
 * 逻辑过期后不删 key，而是由抢到互斥锁的线程异步重建，其余线程继续返回旧数据，
 * 保证热点 key 失效的瞬间不会所有请求一起穿透到数据库（缓存击穿）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisData<T> implements Serializable {

    /** 逻辑过期时间（到点后触发异步重建，而不是让请求失败） */
    private LocalDateTime expireTime;

    private T data;
}