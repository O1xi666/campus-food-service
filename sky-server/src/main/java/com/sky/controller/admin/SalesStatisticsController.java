package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.SalesStatisticsService;
import com.sky.vo.BusinessDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/statistics")
@Api(tags = "管理员销售统计接口")
public class SalesStatisticsController {
    @Autowired
    private SalesStatisticsService salesStatisticsService;

    @PostMapping("/sales/today")
    @ApiOperation("手动统计当日销售额")
    public Result<BusinessDataVO> salesToday() {
        return Result.success(salesStatisticsService.statisticsToday());
    }
}
