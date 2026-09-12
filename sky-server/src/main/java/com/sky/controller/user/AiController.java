package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.AIService;
import com.sky.utils.QwenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;

/**
 * AI 智能服务控制器
 * 提供：智能推荐、经营分析等功能
 */
@RestController
@RequestMapping("/user/ai")
@Api(tags = "AI 智能服务")
@Slf4j
public class AiController {

    @Autowired
    private AIService aiService;

    @Value("${sky.ai.qwen.api-key:}")
    private String configApiKey;

    @PostConstruct
    public void init() {
        if (configApiKey != null && !configApiKey.isEmpty()) {
            QwenUtil.setApiKey(configApiKey);
            log.info("AI 初始化成功：Key 已加载");
        } else {
            log.error("AI 初始化失败：未找到 sky.ai.api-key 配置");
        }
    }

    // ==========================================================
    //  三层 AI 推荐链路：召回 → 排序 → 生成
    // ==========================================================
    @GetMapping("/recommend")
    @ApiOperation("AI 智能点餐推荐（基于用户历史订单画像）")
    public Result<String> recommend(@RequestParam String preference, @RequestParam(required = false) Long merchantId) {
        Long userId = BaseContext.getCurrentId();
        log.info("AI 推荐请求 - userId: {}, 偏好: {}", userId, preference);

        String result = aiService.recommendDishes(userId, preference, merchantId);
        return Result.success(result);
    }

    // ==========================================================
    //  异步经营分析
    // ==========================================================
    @GetMapping("/analysis")
    @ApiOperation("AI 经营日报分析 (异步版)")
    public Result<String> analysis() {
        log.info("触发异步经营分析");
        aiService.runBusinessAnalysis();
        return Result.success("AI分析已启动，请稍后查看结果（当前主线程已释放）");
    }

    @GetMapping("/analysis/sync")
    @ApiOperation("AI 经营日报分析 (同步版)")
    public Result<String> syncAnalysis() {
        log.info("同步请求到达：等待AI分析完成");
        String result = aiService.syncRunBusinessAnalysis();
        return Result.success(result);
    }

    // ==========================================================
    //  页面跳转
    // ==========================================================
    @GetMapping("/page")
    @ApiOperation("跳转到点餐页面")
    public String toOrderPage() {
        return "ai-order";
    }
}

