package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sky.cache.DishBloomFilter;
import com.sky.cache.LogicalExpireCache;
import com.sky.cache.RedisData;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * 菜品服务：缓存三大问题的处理都在这里落地。
 *   穿透 → 布隆过滤器预校验（getById）
 *   击穿 → 逻辑过期 + 互斥锁异步重建（listForUser）
 *   雪崩 → Redis TTL 随机抖动（CacheConfig 里的 JitterRedisCacheWriter）
 *   一致性 → 先更库、再删缓存（updateById / save）
 */
@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    /** 用户端菜单是典型热点 key，用逻辑过期扛住失效瞬间的并发击穿 */
    private static final String USER_DISH_LIST_KEY = "dishCache::user:list";

    @Autowired
    private DishBloomFilter dishBloomFilter;

    @Autowired
    private LogicalExpireCache logicalExpireCache;

    /**
     * 查询单个菜品 → 走二级缓存(本地+Redis)。
     *
     * 缓存未命中时先过布隆过滤器：返回 false 说明这个 id 一定不存在，
     * 直接返回 null，既不查 Redis 也不打数据库 —— 缓存穿透在这里被挡住。
     * （缓存命中时方法体不会执行，所以没有额外开销）
     */
    @Cacheable(value = "dishCache", key = "#id")
    @Override
    public Dish getById(Serializable id) {
        Long dishId = id instanceof Number ? ((Number) id).longValue() : null;
        if (dishId != null && !dishBloomFilter.mightContain(dishId)) {
            log.debug("布隆过滤器拦截不存在的菜品 id: {}", dishId);
            return null;
        }
        return super.getById(id);
    }

    /**
     * 用户端菜单列表 → 逻辑过期缓存。
     * 逻辑过期后由抢到互斥锁的线程异步重建，其余请求先拿旧数据，不会一起打到 DB。
     */
    @Override
    public List<Dish> listForUser() {
        return logicalExpireCache.get(USER_DISH_LIST_KEY,
                new TypeReference<RedisData<List<Dish>>>() {
                },
                this::queryUserDishList);
    }

    private List<Dish> queryUserDishList() {
        return lambdaQuery()
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getUpdateTime)
                .list();
    }

    /**
     * 修改菜品 → 先更库 → 再删缓存（本地缓存 @CacheEvict 清空，逻辑过期 key 手动删）。
     * 顺序不能反：先删缓存再更库，会在"删完还没更库"的窗口里被读到旧值。
     */
    @CacheEvict(value = {"dishCache", "aiRecommendCache"}, allEntries = true)
    @Override
    public boolean updateById(Dish dish) {
        boolean updated = super.updateById(dish);
        if (updated) {
            logicalExpireCache.evict(USER_DISH_LIST_KEY);
        }
        return updated;
    }

    /**
     * 新增菜品 → 写入布隆过滤器 + 删缓存。
     * 注意：布隆过滤器不支持删除元素，菜品被删除后会留下一个"假阳性"，
     * 代价只是这次查询多走一次数据库，不影响正确性。
     */
    @Override
    @CacheEvict(value = {"dishCache", "aiRecommendCache"}, allEntries = true)
    public boolean save(Dish dish) {
        boolean saved = super.save(dish);
        if (saved) {
            dishBloomFilter.add(dish.getId());
            logicalExpireCache.evict(USER_DISH_LIST_KEY);
        }
        return saved;
    }
}