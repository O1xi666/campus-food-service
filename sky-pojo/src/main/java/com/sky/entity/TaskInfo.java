package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务记录表。
 * 异步经营分析用它承载"任务状态 + 结果 + 失败重试 + 幂等键"，
 * 前端拿 taskId 查结果，任务完成后服务端通过 WebSocket 推站内通知。
 */
@Data
@TableName("task_info")
public class TaskInfo {

    public static final String PENDING = "PENDING";
    public static final String RUNNING = "RUNNING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 对外暴露的任务号（UUID），不暴露自增主键 */
    private String taskId;

    /** 任务类型，如 BUSINESS_ANALYSIS */
    private String taskType;

    /** 业务幂等键，如 BUSINESS_ANALYSIS:{merchantId}:{yyyy-MM-dd}，同一键不会重复执行 */
    private String businessKey;

    /** PENDING / RUNNING / SUCCESS / FAILED */
    private String status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    private String result;

    private String errorMsg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}