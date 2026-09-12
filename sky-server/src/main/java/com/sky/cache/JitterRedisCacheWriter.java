package com.sky.cache;

import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 给 Redis 缓存 TTL 加随机抖动的写入器 —— 解决缓存雪崩。
 *
 * 原因：如果同一批缓存 key 在同一时刻写入、TTL 又完全相同，它们会在同一秒集体失效，
 * 请求瞬间全部打到数据库。给每个 key 的 TTL 加一个随机偏移，失效时间就被打散了。
 *
 * 装饰器模式：只改写 put / putIfAbsent 传入的 TTL，其余行为原样委托给默认 writer。
 */
public class JitterRedisCacheWriter implements RedisCacheWriter {

    private final RedisCacheWriter delegate;
    private final long jitterMillis;

    public JitterRedisCacheWriter(RedisCacheWriter delegate, long jitterMillis) {
        this.delegate = delegate;
        this.jitterMillis = Math.max(0L, jitterMillis);
    }

    private Duration applyJitter(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative() || jitterMillis == 0L) {
            return ttl;
        }
        long delta = ThreadLocalRandom.current().nextLong(-jitterMillis, jitterMillis + 1);
        long millis = Math.max(1000L, ttl.toMillis() + delta);
        return Duration.ofMillis(millis);
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        delegate.put(name, key, value, applyJitter(ttl));
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return delegate.get(name, key);
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        return delegate.putIfAbsent(name, key, value, applyJitter(ttl));
    }

    @Override
    public void remove(String name, byte[] key) {
        delegate.remove(name, key);
    }

    @Override
    public void clean(String name, byte[] key) {
        delegate.clean(name, key);
    }

    @Override
    public void clearStatistics(String name) {
        delegate.clearStatistics(name);
    }

    @Override
    public CacheStatistics getCacheStatistics(String name) {
        return delegate.getCacheStatistics(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return new JitterRedisCacheWriter(delegate.withStatisticsCollector(cacheStatisticsCollector), jitterMillis);
    }
}