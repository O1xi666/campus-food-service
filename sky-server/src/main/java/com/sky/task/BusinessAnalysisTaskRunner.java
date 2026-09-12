package com.sky.task;

import com.sky.exception.BusinessException;
import com.sky.service.DataAnalysisService;
import com.sky.service.impl.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步经营分析执行器。
 *
 * 独立成一个 Bean 是因为 @Async 靠 Spring 代理生效 —— 在 AIServiceImpl 内部自调用不会走代理，
 * 那样异步就会"静默失效"变成同步执行。
 *
 * 可靠性设计：
 *   幂等  → 执行前 CAS 抢占任务（PENDING → RUNNING），抢不到说明已有人在跑，直接退出；
 *   重试  → 失败按 1s / 2s / 3s 退避重试，最多 maxRetry 次，每次把 retry_count 落库；
 *   兜底  → 业务异常（如商家不存在）属于不可重试错误，直接标记失败，不浪费重试次数；
 *   通知  → 最终成功或失败都通过 WebSocket 推站内通知给用户。
 */
@Component
@Slf4j
public class BusinessAnalysisTaskRunner {

    private static final long RETRY_BACKOFF_MILLIS = 1000L;

    @Autowired
    private DataAnalysisService dataAnalysisService;

    @Autowired
    private NotificationService notificationService;

    @Async("taskExecutor")
    public void run(Long taskId, Long userId, Long merchantId, int maxRetry) {
        if (!notificationService.claim(taskId)) {
            log.info("任务已被其它线程抢占或已完成，跳过执行（幂等保护）- taskId: {}", taskId);
            return;
        }

        int attempts = Math.max(1, maxRetry);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                log.info("【异步经营分析】第 {}/{} 次执行 - taskId: {}, 线程: {}",
                        attempt, attempts, taskId, Thread.currentThread().getName());
                String report = dataAnalysisService.generateBusinessReport(merchantId);
                notificationService.markSuccess(taskId, report);
                notificationService.notifyUser(userId, "BUSINESS_ANALYSIS", "AI 经营分析已完成",
                        "商家 " + merchantId + " 的经营日报已生成（" + report.length() + " 字），可前往分析页面查看。");
                log.info("【异步经营分析】执行成功 - taskId: {}", taskId);
                return;
            } catch (BusinessException be) {
                log.warn("【异步经营分析】业务校验未通过，不重试 - taskId: {}, 原因: {}", taskId, be.getMessage());
                notificationService.markFailed(taskId, attempt - 1, be.getMessage());
                notificationService.notifyUser(userId, "BUSINESS_ANALYSIS", "AI 经营分析未执行", be.getMessage());
                return;
            } catch (Exception e) {
                log.warn("【异步经营分析】第 {} 次执行失败 - taskId: {}", attempt, taskId, e);
                if (attempt >= attempts) {
                    notificationService.markFailed(taskId, attempt, e.getMessage());
                    notificationService.notifyUser(userId, "BUSINESS_ANALYSIS", "AI 经营分析失败",
                            "已重试 " + attempt + " 次仍未成功：" + e.getMessage());
                    return;
                }
                notificationService.recordRetry(taskId, attempt);
                sleep(RETRY_BACKOFF_MILLIS * attempt);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}