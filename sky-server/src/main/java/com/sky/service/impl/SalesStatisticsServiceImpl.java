package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.entity.TaskInfo;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.TaskInfoMapper;
import com.sky.service.SalesStatisticsService;
import com.sky.vo.BusinessDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SalesStatisticsServiceImpl implements SalesStatisticsService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Override
    public BusinessDataVO statisticsToday() {
        LocalDateTime begin = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        BigDecimal turnover = orderMapper.sumCompletedAmountByTime(begin, end);
        Integer validOrderCount = orderMapper.countCompletedOrdersByTime(begin, end);
        if (turnover == null) {
            turnover = BigDecimal.ZERO;
        }
        if (validOrderCount == null) {
            validOrderCount = 0;
        }

        BusinessDataVO vo = BusinessDataVO.builder()
                .turnover(turnover.doubleValue())
                .validOrderCount(validOrderCount)
                .orderCompletionRate(1.0D)
                .unitPrice(validOrderCount == 0 ? 0D : turnover.doubleValue() / validOrderCount)
                .newUsers(0)
                .build();

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("type", "DAILY_SALES");
        snapshot.put("date", LocalDate.now().toString());
        snapshot.put("turnover", vo.getTurnover());
        snapshot.put("validOrderCount", vo.getValidOrderCount());
        snapshot.put("unitPrice", vo.getUnitPrice());

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setUserId(BaseContext.getCurrentId());
        taskInfo.setTaskId("sales-" + UUID.randomUUID().toString().replace("-", ""));
        taskInfo.setStatus("DONE");
        taskInfo.setResult(JSON.toJSONString(snapshot));
        taskInfo.setCreateTime(LocalDateTime.now());
        taskInfo.setUpdateTime(LocalDateTime.now());
        taskInfoMapper.insert(taskInfo);
        return vo;
    }
}
