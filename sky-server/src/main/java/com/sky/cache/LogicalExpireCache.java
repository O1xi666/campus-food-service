package com.sky.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 逻辑过期缓存（解决缓存击穿）+ 空值缓存（解决缓存穿透）。
 *
 * 读取流程：
 *   1. 命中且未逻辑过期 → 直接返回；
 *   2. 命中但已逻辑过期 → 抢互斥锁，抢到的线程放到后台线程池异步重建，其余线程先返回旧数据；
 *   3. 完全未命中 → 抢锁的线程同步查库并回填，没抢到的线程短暂自旋等待，避免同时打 DB；
 *   4. 查库结果为空 → 写入一个短 TTL 的空值标记，后续请求不再打 DB。
 *
 * 物理 TTL 比逻辑 TTL 长一段，保证逻辑过期时 key 还在，才有"旧数据"可以顶着用。
 * sky.cache.enabled=false（压测基线模式）时全部退化为直接查库。
 */
@Component
@Slf4j
public class LogicalExpireCache {

    private static final String LOCK_SUFFIX = ":rebuild-lock";
    private static final long LOCK_TTL_SECONDS = 10L;
    private static final int SPIN_TIMES = 20;
    private static final long SPIN_INTERVAL_MILLIS = 50L;

    private final ExecutorService rebuildExecutor = new ThreadPoolExecutor(
            2, 4, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(64),
            runnable -> {
                Thread thread = new Thread(runnable, "cache-rebuild-" + System.nanoTime() % 1000);
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${sky.cache.enabled:true}")
    private boolean cacheEnabled;

    /** 逻辑过期时间：到点后触发后台重建 */
    @Value("${sky.cache.redis.logical-ttl-seconds:180}")
    private long logicalTtlSeconds;

    /** 空值缓存 / 额外的物理 TTL 冗余 */
    @Value("${sky.cache.redis.null-cache-ttl-seconds:60}")
    private long nullCacheTtlSeconds;

    @PreDestroy
    public void shutdown() {
        rebuildExecutor.shutdownNow();
    }

    public <T> T get(String key, TypeReference<RedisData<T>> typeRef, Supplier<T> loader) {
        if (!cacheEnabled) {
            return loader.get();
        }
        try {
            RedisData<T> cached = read(key, typeRef);
            if (cached == null) {
                return loadWithMutex(key, typeRef, loader);
            }
            if (cached.getExpireTime() == null || cached.getExpireTime().isAfter(LocalDateTime.now())) {
                return cached.getData();
            }
            // 已逻辑过期：拿到锁的线程异步重建，拿不到的先用旧数据兜住
            if (tryLock(key)) {
                submitRebuild(key, loader);
            }
            return cached.getData();
        } catch (Exception e) {
            log.error("逻辑过期缓存读取异常，降级为直接查库 - key: {}", key, e);
            return loader.get();
        }
    }

    /** 缓存删除（配合"先更库、再删缓存"的一致性策略） */
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("删除缓存失败 - key: {}", key, e);
        }
    }

    private <T> T loadWithMutex(String key, TypeReference<RedisData<T>> typeRef, Supplier<T> loader) throws Exception {
        if (!tryLock(key)) {
            for (int i = 0; i < SPIN_TIMES; i++) {
                Thread.sleep(SPIN_INTERVAL_MILLIS);
                RedisData<T> cached = read(key, typeRef);
                if (cached != null) {
                    return cached.getData();
                }
            }
            log.warn("等待缓存重建超时，本次直接查库兜底 - key: {}", key);
            return loader.get();
        }
        try {
            // 双重检查：抢到锁后再看一眼，避免前一个持锁线程已经回填过
            RedisData<T> cached = read(key, typeRef);
            if (cached != null) {
                return cached.getData();
            }
            T data = loader.get();
            write(key, data);
            return data;
        } finally {
            unlock(key);
        }
    }

    private <T> void submitRebuild(String key, Supplier<T> loader) {
        rebuildExecutor.submit(() -> {
            try {
                write(key, loader.get());
                log.debug("逻辑过期 key 异步重建完成 - {}", key);
            } catch (Exception e) {
                log.error("逻辑过期 key 异步重建失败 - {}", key, e);
            } finally {
                unlock(key);
            }
        });
    }

    private <T> void write(String key, T data) {
        long logicalTtl = data == null ? nullCacheTtlSeconds : logicalTtlSeconds;
        RedisData<T> wrapper = new RedisData<>(LocalDateTime.now().plusSeconds(logicalTtl), data);
        try {
            String json = objectMapper.writeValueAsString(wrapper);
            redisTemplate.opsForValue().set(key, json,
                    Duration.ofSeconds(logicalTtl + nullCacheTtlSeconds));
        } catch (Exception e) {
            log.error("写入逻辑过期缓存失败 - key: {}", key, e);
        }
    }

    private <T> RedisData<T> read(String key, TypeReference<RedisData<T>> typeRef) throws Exception {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            return null;
        }
        return objectMapper.readValue(raw.toString(), typeRef);
    }

    private boolean tryLock(String key) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key + LOCK_SUFFIX, "1", Duration.ofSeconds(LOCK_TTL_SECONDS));
        return Boolean.TRUE.equals(success);
    }

    private void unlock(String key) {
        try {
            redisTemplate.delete(key + LOCK_SUFFIX);
        } catch (Exception e) {
            log.warn("释放重建锁失败 - key: {}", key, e);
        }
    }
}