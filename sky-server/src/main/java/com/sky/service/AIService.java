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
     * 提交异步经营分析任务。
     *
     * 注意：这里只负责"建任务 + 派发"，真正的执行在 BusinessAnalysisTaskRunner 里由线程池异步完成，
     * 所以方法本身是同步返回的，能把 taskId 立刻给到前端。
     *
     * @param userId 用户ID（异步线程拿不到 ThreadLocal，必须显式传入）
     * @param merchantId 商家ID，为空时按用户历史订单推断
     * @return 任务号 taskId，前端可用它查询进度
     */
    String runBusinessAnalysis(Long userId, Long merchantId);

    /**
     * 同步执行经营分析任务
     */
    String syncRunBusinessAnalysis(Long userId, Long merchantId);
}