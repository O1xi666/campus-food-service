package com.sky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.entity.TaskInfo;
import com.sky.mapper.TaskInfoMapper;
import com.sky.websocket.NotificationWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 异步任务 + 站内通知的公共能力。
 *
 * 幂等设计：
 *   1. 提交前用 businessKey（业务维度唯一键）查一次，PENDING/RUNNING/SUCCESS 直接复用，不重复提交；
 *   2. 执行前用 CAS（UPDATE ... WHERE status='PENDING'）抢任务，抢不到的线程直接退出，
 *      即使重复提交也只有一个线程真正执行。
 */
@Component
@Slf4j
public class NotificationService {

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    /** 按幂等键获取或创建任务 */
    public TaskInfo getOrCreateTask(Long userId, String taskType, String businessKey, int maxRetry) {
        TaskInfo existing = taskInfoMapper.selectOne(
                Wrappers.<TaskInfo>lambdaQuery()
                        .eq(TaskInfo::getBusinessKey, businessKey)
                        .orderByDesc(TaskInfo::getId)
                        .last("LIMIT 1"));
        if (existing != null) {
            boolean finished = TaskInfo.SUCCESS.equals(existing.getStatus());
            boolean inFlight = TaskInfo.PENDING.equals(existing.getStatus())
                    || TaskInfo.RUNNING.equals(existing.getStatus());
            if (finished || inFlight) {
                log.info("命中幂等键，复用已有任务 - businessKey: {}, status: {}",
                        businessKey, existing.getStatus());
                return existing;
            }
        }

        TaskInfo task = new TaskInfo();
        task.setUserId(userId);
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setTaskType(taskType);
        task.setBusinessKey(businessKey);
        task.setStatus(TaskInfo.PENDING);
        task.setRetryCount(0);
        task.setMaxRetry(maxRetry);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskInfoMapper.insert(task);
        log.info("创建异步任务 - taskId: {}, type: {}, businessKey: {}",
                task.getTaskId(), taskType, businessKey);
        return task;
    }

    /** CAS 抢占：只有把 PENDING 改成 RUNNING 成功的线程才执行，保证同一任务只跑一次 */
    public boolean claim(Long taskId) {
        int rows = taskInfoMapper.update(null, Wrappers.<TaskInfo>lambdaUpdate()
                .eq(TaskInfo::getId, taskId)
                .eq(TaskInfo::getStatus, TaskInfo.PENDING)
                .set(TaskInfo::getStatus, TaskInfo.RUNNING)
                .set(TaskInfo::getUpdateTime, LocalDateTime.now()));
        return rows > 0;
    }

    public void recordRetry(Long taskId, int retryCount) {
        taskInfoMapper.update(null, Wrappers.<TaskInfo>lambdaUpdate()
                .eq(TaskInfo::getId, taskId)
                .set(TaskInfo::getRetryCount, retryCount)
                .set(TaskInfo::getUpdateTime, LocalDateTime.now()));
    }

    public void markSuccess(Long taskId, String result) {
        taskInfoMapper.update(null, Wrappers.<TaskInfo>lambdaUpdate()
                .eq(TaskInfo::getId, taskId)
                .set(TaskInfo::getStatus, TaskInfo.SUCCESS)
                .set(TaskInfo::getResult, result)
                .set(TaskInfo::getErrorMsg, null)
                .set(TaskInfo::getUpdateTime, LocalDateTime.now()));
    }

    public void markFailed(Long taskId, int retryCount, String errorMsg) {
        String truncated = errorMsg != null && errorMsg.length() > 250
                ? errorMsg.substring(0, 250) : errorMsg;
        taskInfoMapper.update(null, Wrappers.<TaskInfo>lambdaUpdate()
                .eq(TaskInfo::getId, taskId)
                .set(TaskInfo::getStatus, TaskInfo.FAILED)
                .set(TaskInfo::getRetryCount, retryCount)
                .set(TaskInfo::getErrorMsg, truncated)
                .set(TaskInfo::getUpdateTime, LocalDateTime.now()));
    }

    /** 站内通知：通过 WebSocket 推给在线用户，不在线就只落日志 */
    public void notifyUser(Long userId, String type, String title, String content) {
        NotificationWebSocketServer.sendToUser(userId, type, title, content);
    }
}