package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.DataAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/analysis")
@Api(tags = "数据分析接口")
@Slf4j
public class DataAnalysisController {

    @Autowired
    private DataAnalysisService dataAnalysisService;

    @GetMapping("/report")
    @ApiOperation("AI 经营分析报告 - 自动从登录上下文获取商家")
    public Result<String> getBusinessReport() {
        try {
            String report = dataAnalysisService.generateBusinessReport();
            return Result.success(report);
        } catch (Exception e) {
            log.error("生成分析报告异常", e);
            return Result.error("生成分析报告失败：" + e.getMessage());
        }
    }
}
