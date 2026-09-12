package com.sky.service;

/**
 * 数据分析服务接口（从登录上下文获取商家）
 */
public interface DataAnalysisService {

    /**
     * 生成商家经营分析报告
     * @return AI 生成的分析报告文本
     */
    String generateBusinessReport();

    /**
     * 生成指定商家的经营分析报告
     * 异步任务 / 定时任务没有 ThreadLocal 上下文，必须显式传入 merchantId
     * @param merchantId 商家ID
     * @return AI 生成的分析报告文本
     */
    String generateBusinessReport(Long merchantId);
}
