package com.sky.cache;

import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 菜品 id 布隆过滤器。
 *
 * 启动时用全量菜品 id 预热，新增菜品时追加写入；
 * 查询单个菜品前先过一遍 mightContain，返回 false 直接判定"不存在"，
 * 不再查 Redis、也不打 DB —— 挡住缓存穿透。
 *
 * 规模取舍：单机预热 + 写时追加，对课程项目的菜品量级完全够用；
 * 多实例部署时可以把位数组换成 Redis Bitmap（SETBIT / GETBIT）做共享。
 */
@Component
@Slf4j
public class DishBloomFilter {

    /** n = 1 万，误判率 1%：位数组约 9.6 万 bit（≈12KB），哈希函数 7 个 */
    private final BloomFilter filter = new BloomFilter(10_000, 0.01);

    @Autowired
    private DishMapper dishMapper;

    @Value("${sky.cache.bloom.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void warmUp() {
        if (!enabled) {
            log.warn("布隆过滤器已关闭（sky.cache.bloom.enabled=false）");
            return;
        }
        rebuild();
    }

    public synchronized void rebuild() {
        List<Dish> dishes = dishMapper.selectList(null);
        List<Long> ids = dishes == null ? Collections.emptyList()
                : dishes.stream().map(Dish::getId).filter(Objects::nonNull).collect(Collectors.toList());
        ids.forEach(filter::add);
        log.info("布隆过滤器预热完成 - 元素={} 个, 位数组={} bit, 哈希函数={} 个, 占用≈{} KB",
                ids.size(), filter.bitCount(), filter.hashFunctions(), filter.bitCount() / 8 / 1024);
    }

    public void add(Long dishId) {
        if (enabled && dishId != null) {
            filter.add(dishId);
        }
    }

    /** true = 可能存在（继续走缓存 / 查库）；false = 一定不存在（直接拦截） */
    public boolean mightContain(Long dishId) {
        if (!enabled || dishId == null) {
            return true;
        }
        return filter.mightContain(dishId);
    }
}