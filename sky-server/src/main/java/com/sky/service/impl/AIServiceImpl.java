package com.sky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.BusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.service.AIService;
import com.sky.utils.QwenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIServiceImpl implements AIService {

    private static final int TOP_K = 10;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ======================== 三层 AI 推荐链路 ========================

    @Override
    public String recommendDishes(Long userId, String preference, Long merchantId) {
        if (preference == null || preference.trim().isEmpty()) {
            preference = "随便吃点";
        }

        // 如果前端没传 merchantId，尝试从用户最近订单推断
        if (merchantId == null) {
            merchantId = inferMerchantFromHistory(userId);
        }

        // 仍然没有 merchantId（新用户 + 前端没传）→ 提示
        if (merchantId == null) {
            // 返回所有启售菜品做通用推荐（兼容旧前端）
            log.warn("merchantId 未提供且无法从历史推断，返回全平台菜品");
        }

        // ----- 第一层：召回 —— 构建用户画像 -----
        log.info("【召回】开始构建用户画像 - userId: {}, merchantId: {}", userId, merchantId);
        UserProfile profile = buildUserProfile(userId, merchantId);
        log.info("【召回】用户画像完成 - 历史订单: {} 单", profile.totalOrders);

        // ----- 第二层：排序 —— 获取推荐菜品 -----
        log.info("【排序】获取推荐菜品列表");
        List<Dish> recommendedDishes;

        if (profile.totalOrders == 0) {
            if (merchantId != null) {
                log.info("用户无历史记录，走商家 {} 销量榜单推荐", merchantId);
                recommendedDishes = loadTopKDishes(merchantId, TOP_K);
            } else {
                log.info("用户无历史记录且无商家，走全平台菜品");
                recommendedDishes = dishMapper.selectList(
                        Wrappers.<Dish>lambdaQuery().eq(Dish::getStatus, 1)
                );
            }
            profile.isNewUser = true;
        } else {
            recommendedDishes = loadRankedDishes(profile, merchantId);
        }

        // ----- 第三层：生成 —— 构建 Prompt 调用大模型 -----
        log.info("【生成】构建 Prompt 并调用大模型");
        String prompt = buildRecommendPrompt(profile, recommendedDishes, preference);

        String reply = QwenUtil.chat(prompt);
        log.info("【生成】推荐完成");
        return reply;
    }

    /** 从用户最近完成订单推断 merchantId */
    private Long inferMerchantFromHistory(Long userId) {
        List<Orders> recent = orderMapper.selectList(
                Wrappers.<Orders>lambdaQuery()
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getStatus, Orders.COMPLETED)
                        .orderByDesc(Orders::getOrderTime)
                        .last("LIMIT 1")
        );
        if (recent != null && !recent.isEmpty()) {
            Long merchantId = recent.get(0).getMerchantId();
            if (merchantId != null) {
                log.info("从最近订单推断 merchantId: {}", merchantId);
                return merchantId;
            }
        }
        return null;
    }

    // ======================== 召回：用户画像 ========================

    private UserProfile buildUserProfile(Long userId, Long merchantId) {
        List<Orders> orders;
        if (merchantId != null) {
            orders = orderMapper.selectList(
                    Wrappers.<Orders>lambdaQuery()
                            .eq(Orders::getUserId, userId)
                            .eq(Orders::getStatus, Orders.COMPLETED)
                            .eq(Orders::getMerchantId, merchantId)
                            .orderByDesc(Orders::getOrderTime)
            );
        } else {
            orders = orderMapper.selectList(
                    Wrappers.<Orders>lambdaQuery()
                            .eq(Orders::getUserId, userId)
                            .eq(Orders::getStatus, Orders.COMPLETED)
                            .orderByDesc(Orders::getOrderTime)
            );
        }

        Map<Long, Integer> dishFreq = new HashMap<>();
        Map<Long, String> dishNameMap = new HashMap<>();
        Map<String, Integer> flavorFreq = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Orders order : orders) {
            totalAmount = totalAmount.add(order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO);
            List<OrderDetail> details = orderDetailMapper.getByOrderId(order.getId());
            for (OrderDetail detail : details) {
                Long dishId = detail.getDishId();
                if (dishId != null) {
                    dishNameMap.put(dishId, detail.getName());
                    dishFreq.merge(dishId, detail.getNumber(), Integer::sum);
                }
                if (detail.getDishFlavor() != null && !detail.getDishFlavor().isEmpty()) {
                    flavorFreq.merge(detail.getDishFlavor().trim(), detail.getNumber(), Integer::sum);
                }
            }
        }

        List<Map.Entry<Long, Integer>> sortedDishes = dishFreq.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        List<Map.Entry<String, Integer>> sortedFlavors = flavorFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        double avgPrice = orders.isEmpty() ? 0 : totalAmount.doubleValue() / orders.size();

        UserProfile profile = new UserProfile();
        profile.totalOrders = orders.size();
        profile.totalAmount = totalAmount;
        profile.avgPrice = avgPrice;
        profile.topDishes = sortedDishes.stream()
                .limit(3)
                .map(e -> dishNameMap.getOrDefault(e.getKey(), "菜品" + e.getKey())
                        + "(" + e.getValue() + "次)")
                .collect(Collectors.toList());
        profile.topFlavors = sortedFlavors.stream()
                .limit(3)
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        profile.dishFreq = dishFreq;
        return profile;
    }

    // ======================== 排序：Redis zset 销量 Top K（无历史记录时使用） ========================

    private List<Dish> loadTopKDishes(Long merchantId, int k) {
        String rankKey = "dish:sales:rank:" + merchantId;
        Set<Object> topIds = redisTemplate.opsForZSet()
                .reverseRange(rankKey, 0, k - 1);

        // zset 为空 → 兜底
        if (topIds == null || topIds.isEmpty()) {
            log.warn("商家 {} 销量排行榜为空，回退到全部菜品", merchantId);
            return dishMapper.selectList(
                    Wrappers.<Dish>lambdaQuery()
                            .eq(Dish::getStatus, 1)
                            .eq(Dish::getMerchantId, merchantId)
            );
        }

        List<Long> ids = topIds.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());
        List<Dish> dishes = dishMapper.selectList(
                Wrappers.<Dish>lambdaQuery()
                        .in(Dish::getId, ids)
                        .eq(Dish::getStatus, 1)
        );

        Map<Long, Dish> dishMap = dishes.stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));
        List<Dish> result = new ArrayList<>();
        for (Object idStr : topIds) {
            Dish d = dishMap.get(Long.parseLong(idStr.toString()));
            if (d != null) result.add(d);
        }
        return result;
    }

    // ======================== 排序：按用户画像预排序（有历史记录时使用） ========================

    private List<Dish> loadRankedDishes(UserProfile profile, Long merchantId) {
        List<Dish> dishes;
        if (merchantId != null) {
            dishes = dishMapper.selectList(
                    Wrappers.<Dish>lambdaQuery()
                            .eq(Dish::getStatus, 1)
                            .eq(Dish::getMerchantId, merchantId)
            );
        } else {
            dishes = dishMapper.selectList(
                    Wrappers.<Dish>lambdaQuery().eq(Dish::getStatus, 1)
            );
        }
        if (dishes == null) return new ArrayList<>();

        dishes.sort((a, b) -> {
            int freqA = profile.dishFreq.getOrDefault(a.getId(), 0);
            int freqB = profile.dishFreq.getOrDefault(b.getId(), 0);
            if (freqA != freqB) return Integer.compare(freqB, freqA);
            double diffA = Math.abs(a.getPrice().doubleValue() - profile.avgPrice);
            double diffB = Math.abs(b.getPrice().doubleValue() - profile.avgPrice);
            return Double.compare(diffA, diffB);
        });
        return dishes;
    }

    // ======================== 生成：构建 Prompt ========================

    private String buildRecommendPrompt(UserProfile profile, List<Dish> dishes, String preference) {
        DecimalFormat df = new DecimalFormat("#0.00");
        StringBuilder sb = new StringBuilder();

        if (profile.isNewUser) {
            sb.append("【推荐来源】全平台销量榜单\n");
            sb.append("该用户首次使用推荐功能，展示当前最受欢迎的菜品。\n");
        } else {
            sb.append("【用户画像】（从历史订单分析得出）\n");
            sb.append("- 历史完成订单数：").append(profile.totalOrders).append(" 单\n");
            sb.append("- 累计消费金额：").append(df.format(profile.totalAmount)).append(" 元\n");
            sb.append("- 平均客单价：约 ").append(df.format(profile.avgPrice)).append(" 元\n");
            if (!profile.topDishes.isEmpty()) {
                sb.append("- 最爱菜品 Top3：").append(String.join("、", profile.topDishes)).append("\n");
            }
            if (!profile.topFlavors.isEmpty()) {
                sb.append("- 口味偏好：").append(String.join("、", profile.topFlavors)).append("\n");
            }
        }
        sb.append("\n");

        sb.append("【今日可点菜单】\n");
        for (int i = 0; i < dishes.size(); i++) {
            Dish d = dishes.get(i);
            String tag = "";
            if (!profile.isNewUser && profile.dishFreq.containsKey(d.getId())) {
                tag = " ★已点过";
            }
            String desc = d.getDescription() != null && !d.getDescription().isEmpty()
                    ? " - " + d.getDescription() : "";
            sb.append((i + 1)).append(". ").append(d.getName())
                    .append("（").append(d.getPrice()).append(" 元）")
                    .append(tag).append(desc).append("\n");
        }
        sb.append("\n");

        sb.append("【用户本次需求】\n").append(preference).append("\n\n");

        sb.append("【任务】\n");
        sb.append("你是一名专业的学生餐厅点餐助手。\n");
        sb.append("请从【今日可点菜单】中精选 3 道最适合用户的菜品：\n");
        sb.append("1. 按推荐优先级从高到低排序\n");
        sb.append("2. 每道菜给出个性化推荐理由（结合用户偏好）\n");
        sb.append("3. 最后给出简短总结\n");
        sb.append("用自然语言回复，直接给推荐内容。");

        return sb.toString();
    }

    // ======================== 用户画像内部类 ========================

    private static class UserProfile {
        int totalOrders;
        BigDecimal totalAmount = BigDecimal.ZERO;
        double avgPrice;
        List<String> topDishes = new ArrayList<>();
        List<String> topFlavors = new ArrayList<>();
        Map<Long, Integer> dishFreq = new HashMap<>();
        boolean isNewUser;
    }

    // ======================== 原有的异步经营分析 ========================

    private static final String MOCK_ORDER_STATS =
            "【昨日经营日报】\n" +
                    "- 总订单量：58 单\n" +
                    "- 总营业额：1,280 元\n" +
                    "- 客单价：27.1 元\n" +
                    "- 异常订单：5 单（3 单超时，2 单退款）\n" +
                    "- 热销 Top1: 宫保鸡丁 (42 份)\n" +
                    "- 滞销 Top1: 清炒时蔬 (5 份)";

    @Async
    @Override
    public void runBusinessAnalysis() {
        log.info("开始异步经营分析任务(线程名: {})", Thread.currentThread().getName());
        try {
            String prompt = "你是一位资深餐饮数据分析师。以下是昨天的经营数据：\n"
                    + MOCK_ORDER_STATS
                    + "\n\n请完成以下任务：\n1. 总结亮点。\n2. 指出潜在风险。\n3. 给出一条具体改进建议。\n请用条理清晰的格式返回。";
            String reply = QwenUtil.chat(prompt);
            log.info("AI 分析完成，结果如下：\n{}", reply);
        } catch (Exception e) {
            log.error("经营分析任务执行失败", e);
        }
    }

    @Override
    public String syncRunBusinessAnalysis() {
        log.info("开始同步经营分析任务(线程名: {})", Thread.currentThread().getName());
        try {
            String prompt = "你是一位资深餐饮数据分析师。以下是昨天的经营数据：\n"
                    + MOCK_ORDER_STATS
                    + "\n\n请完成以下任务：\n1. 总结亮点。\n2. 指出潜在风险。\n3. 给出一条具体改进建议。\n请用条理清晰的格式返回。";
            String reply = QwenUtil.chat(prompt);
            log.info("AI 分析完成，结果如下：\n{}", reply);
            return reply;
        } catch (Exception e) {
            log.error("经营分析任务执行失败", e);
            return "AI分析失败，请稍后重试";
        }
    }
}
