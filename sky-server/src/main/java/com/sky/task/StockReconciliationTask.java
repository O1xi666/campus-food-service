package com.sky.task;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StockReconciliationTask {

    private static final String STOCK_KEY_PREFIX = "dish:stock:";
    private static final long LARGE_GAP_THRESHOLD = 50;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrderMapper orderMapper;

    @PostConstruct
    public void initSalesRank() {
        log.info("初始化 Redis 菜品销量排行榜（per-merchant）...");
        try {
            List<GoodsSalesDTO> salesData = orderMapper.selectAllSalesRank();
            if (salesData == null || salesData.isEmpty()) {
                log.warn("无历史销量数据，排行榜初始化为空");
                return;
            }

            Map<Long, List<GoodsSalesDTO>> byMerchant = salesData.stream()
                    .filter(dto -> dto.getMerchantId() != null)
                    .collect(Collectors.groupingBy(GoodsSalesDTO::getMerchantId));

            for (Map.Entry<Long, List<GoodsSalesDTO>> entry : byMerchant.entrySet()) {
                String rankKey = "dish:sales:rank:" + entry.getKey();
                redisTemplate.delete(rankKey);
                for (GoodsSalesDTO item : entry.getValue()) {
                    if (item.getDishId() != null) {
                        redisTemplate.opsForZSet().add(
                                rankKey,
                                item.getDishId().toString(),
                                item.getNumber().doubleValue()
                        );
                    }
                }
                log.info("商家 {} 排行榜初始化完成，{} 条记录", entry.getKey(), entry.getValue().size());
            }
            log.info("Redis 销量排行榜初始化完成，共 {} 个商家", byMerchant.size());
        } catch (Exception e) {
            log.error("初始化销量排行榜失败，不影响系统运行", e);
        }
    }

    @Scheduled(fixedRate = 300000)
    public void syncSalesRank() {
        log.debug("开始同步 Redis 销量排行榜（全量覆盖）...");
        try {
            List<GoodsSalesDTO> salesData = orderMapper.selectAllSalesRank();
            if (salesData == null || salesData.isEmpty()) return;

            Map<Long, List<GoodsSalesDTO>> byMerchant = salesData.stream()
                    .filter(dto -> dto.getMerchantId() != null)
                    .collect(Collectors.groupingBy(GoodsSalesDTO::getMerchantId));

            for (Map.Entry<Long, List<GoodsSalesDTO>> entry : byMerchant.entrySet()) {
                String rankKey = "dish:sales:rank:" + entry.getKey();
                redisTemplate.delete(rankKey);
                for (GoodsSalesDTO item : entry.getValue()) {
                    if (item.getDishId() != null) {
                        redisTemplate.opsForZSet().add(
                                rankKey,
                                item.getDishId().toString(),
                                item.getNumber().doubleValue()
                        );
                    }
                }
            }
            log.debug("销量排行榜同步完成，{} 个商家", byMerchant.size());
        } catch (Exception e) {
            log.error("销量排行榜同步失败", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void reconcileStock() {
        log.debug("开始库存对账...");
        List<Dish> dishes = dishMapper.selectList(null);
        if (dishes == null || dishes.isEmpty()) return;

        int mismatchCount = 0;
        for (Dish dish : dishes) {
            String redisKey = STOCK_KEY_PREFIX + dish.getId();
            Object redisStockObj = redisTemplate.opsForValue().get(redisKey);
            if (redisStockObj == null) continue;

            long redisStock = Long.parseLong(redisStockObj.toString());
            int mysqlStock = dish.getStock() != null ? dish.getStock() : 0;

            if (redisStock == mysqlStock) continue;
            mismatchCount++;

            if (redisStock > mysqlStock) {
                log.warn("库存偏高: dishId={}, Redis={}, MySQL={}。同步 Redis → MySQL", dish.getId(), redisStock, mysqlStock);
                redisTemplate.opsForValue().set(redisKey, String.valueOf(mysqlStock));
            } else {
                long gap = mysqlStock - redisStock;
                if (gap > LARGE_GAP_THRESHOLD) {
                    log.warn("库存缺口偏大: dishId={}, Redis={}, MySQL={}, 缺口={}", dish.getId(), redisStock, mysqlStock, gap);
                }
            }
        }
        if (mismatchCount > 0) {
            log.info("库存对账完成，{} 个菜品存在偏差", mismatchCount);
        }
    }
}
