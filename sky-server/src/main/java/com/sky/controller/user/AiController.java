package com.sky.controller.user;

import com.sky.annotation.RateLimit;
import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.AIService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // ==========================================================
    //  三层 AI 推荐链路：召回 → 排序 → 生成
    // ==========================================================
    /**
     * 大模型调用成本高、耗时也长，这里用滑动窗口限流做保护：
     * 全站 600 次/分钟，单 IP 30 次/分钟，单用户 20 次/分钟，任一维度超限直接拒绝。
     */
    @GetMapping("/recommend")
    @ApiOperation("AI 智能点餐推荐（基于用户历史订单画像）")
    @RateLimit(key = "ai:recommend", limit = 30, window = 60,
            dimensions = {RateLimit.Dimension.GLOBAL, RateLimit.Dimension.IP, RateLimit.Dimension.USER})
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
    public Result<String> analysis(@RequestParam(required = false) Long merchantId) {
        Long userId = BaseContext.getCurrentId();
        log.info("触发异步经营分析 - userId: {}, merchantId: {}", userId, merchantId);
        String taskId = aiService.runBusinessAnalysis(userId, merchantId);
        return Result.success("AI分析已启动，任务号 " + taskId + "，完成后会通过站内通知推送结果");
    }

    @GetMapping("/analysis/sync")
    @ApiOperation("AI 经营日报分析 (同步版)")
    public Result<String> syncAnalysis(@RequestParam(required = false) Long merchantId) {
        Long userId = BaseContext.getCurrentId();
        log.info("同步请求到达：等待AI分析完成 - userId: {}, merchantId: {}", userId, merchantId);
        return Result.success(aiService.syncRunBusinessAnalysis(userId, merchantId));
    }

    // ==========================================================
    //  页面跳转
    // ==========================================================
    /**
     * 跳转到点餐页。
     *
     * 注意：@RestController 会把 String 返回值直接当成响应体写出去，
     * 所以这里必须用重定向，返回视图名只会得到字符串 "ai-order"。
     */
    @GetMapping("/page")
    @ApiOperation("跳转到点餐页面")
    public void toOrderPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("/ai-order.html");
    }
}