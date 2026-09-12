package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_info")
public class TaskInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String taskId;
    private String status;
    private String result;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

