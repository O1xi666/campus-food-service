package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sky.cache.JitterRedisCacheWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;

/**
 * 二级缓存配置：L1 Caffeine 本地缓存 + L2 Redis 缓存。
 *
 * 读取顺序由 {@link CompositeCacheManager} 决定：先查 L1，未命中再查 L2；
 * 写入时两层同时写，淘汰时两层同时删，因此不存在"本地缓存读到旧值、Redis 已是新值"的窗口。
 *
 * Redis 层的 TTL 由 {@link JitterRedisCacheWriter} 加了随机抖动，避免同批 key 同时失效（缓存雪崩）。
 *
 * sky.cache.enabled=false 时整体降级为 NoOpCacheManager：
 * 所有 @Cacheable 直接穿透到数据库，用于压测取"无缓存"基线，生产上也可以作为缓存故障时的降级开关。
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /** L1：本地缓存（Caffeine）。不固定 cacheNames，业务用到哪个缓存名就动态创建。 */
    @Bean
    public CaffeineCacheManager localCacheManager(
            @Value("${sky.cache.local.maximum-size:1000}") long maximumSize,
            @Value("${sky.cache.local.expire-seconds:300}") long expireSeconds) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(64)
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofSeconds(expireSeconds))
                .recordStats());
        log.info("本地缓存(Caffeine)初始化完成 - maximumSize={}, expireAfterWrite={}s", maximumSize, expireSeconds);
        return cacheManager;
    }

    /** L2：Redis 缓存（TTL 带随机抖动，防雪崩） */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                               @Value("${sky.cache.redis.time-to-live:1800000}") long ttlMillis,
                                               @Value("${sky.cache.redis.ttl-jitter-ms:300000}") long ttlJitterMillis,
                                               @Value("${sky.cache.redis.cache-null-values:false}") boolean cacheNullValues) {
        ObjectMapper om = new ObjectMapper();
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .entryTtl(Duration.ofMillis(ttlMillis));
        config = cacheNullValues ? config : config.disableCachingNullValues();

        // 装饰器：给每个 key 的 TTL 叠加 [-jitter, +jitter] 的随机偏移
        RedisCacheWriter cacheWriter = new JitterRedisCacheWriter(
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), ttlJitterMillis);

        log.info("Redis 缓存(L2)初始化完成 - ttl={}ms, ttlJitter=±{}ms, cacheNullValues={}",
                ttlMillis, ttlJitterMillis, cacheNullValues);
        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(config)
                .build();
    }

    /** 组合管理器：L1 优先，L2 兜底；开关关闭时退化为无缓存 */
    @Bean
    @Primary
    public CacheManager cacheManager(
            @Value("${sky.cache.enabled:true}") boolean cacheEnabled,
            CaffeineCacheManager localCacheManager,
            RedisCacheManager redisCacheManager) {
        if (!cacheEnabled) {
            log.warn("sky.cache.enabled=false —— 缓存已关闭，所有查询直接回源数据库（压测基线模式）");
            return new NoOpCacheManager();
        }
        CompositeCacheManager composite = new CompositeCacheManager();
        composite.setCacheManagers(Arrays.asList(localCacheManager, redisCacheManager));
        return composite;
    }
}