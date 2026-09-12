package com.sky.service;

public interface AIService {

    /**
     * AI 智能菜品推荐：召回-排序-生成 三层链路
     * @param userId 用户ID
     * @param preference 用户本次输入的需求描述
     * @return 自然语言推荐结果
     */
    String recommendDishes(Long userId, String preference, Long merchantId);

    /**
     * 异步执行经营分析任务
     */
    void runBusinessAnalysis();

    String syncRunBusinessAnalysis();
}

