package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Merchant;
import com.sky.context.BaseContext;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BusinessException;
import com.sky.mapper.MerchantMapper;
import com.sky.mapper.OrderMapper;
import com.sky.service.DataAnalysisService;
import com.sky.utils.QwenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Service
@Slf4j
public class DataAnalysisServiceImpl implements DataAnalysisService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public String generateBusinessReport() {
        // 1. 从登录上下文获取商家ID
        Long merchantId = BaseContext.getCurrentMerchantId();
        Merchant merchant = merchantMapper.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        log.info("开始为商家 [{}] {} 生成经营分析报告", merchantId, merchant.getName());

        // 2. 从数据库查询销售数据
        BigDecimal totalSales = orderMapper.sumTotalSales(merchantId);
        Integer totalOrders = orderMapper.countTotalOrders(merchantId);
        List<GoodsSalesDTO> top5 = orderMapper.selectSalesTop5(merchantId);
        List<GoodsSalesDTO> bottom5 = orderMapper.selectSalesBottom5(merchantId);

        if (totalSales == null) totalSales = BigDecimal.ZERO;
        if (totalOrders == null) totalOrders = 0;

        // 3. 计算平均客单价
        double avgPrice = totalOrders > 0
                ? totalSales.doubleValue() / totalOrders
                : 0.0;

        // 4. 将数据格式化为自然语言
        String dataText = formatDataToText(totalSales, totalOrders, avgPrice, top5, bottom5);

        // 5. 构造 AI prompt
        String prompt = "你是一位资深的餐饮数据分析师。以下是一家餐厅的经营数据：\n\n"
                + dataText
                + "\n\n请完成以下任务：\n"
                + "1. 总结整体经营情况，分析亮点和趋势\n"
                + "2. 指出潜在风险或问题\n"
                + "3. 针对菜品销售情况给出具体改进建议\n"
                + "4. 给出 1-2 条可执行的运营策略\n\n"
                + "请用条理清晰的格式返回，语言简洁专业。";

        // 6. 调用阿里云大模型
        log.info("调用阿里云大模型生成分析报告...");
        String reply = QwenUtil.chat(prompt);
        log.info("AI 分析完成");

        return reply;
    }

    /**
     * 将查询结果格式化为自然语言文本
     */
    private String formatDataToText(BigDecimal totalSales, Integer totalOrders,
                                    double avgPrice, List<GoodsSalesDTO> top5,
                                    List<GoodsSalesDTO> bottom5) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        StringBuilder sb = new StringBuilder();

        sb.append("【经营概况】\n");
        sb.append("- 总销售额：").append(df.format(totalSales)).append(" 元\n");
        sb.append("- 总订单数：").append(totalOrders).append(" 单\n");
        sb.append("- 平均客单价：").append(df.format(avgPrice)).append(" 元/单\n\n");

        sb.append("【热销 TOP5 菜品】\n");
        if (top5 != null && !top5.isEmpty()) {
            for (int i = 0; i < top5.size(); i++) {
                GoodsSalesDTO dto = top5.get(i);
                sb.append("  ").append(i + 1).append(". ")
                        .append(dto.getName())
                        .append(" - 销量 ").append(dto.getNumber()).append(" 份\n");
            }
        } else {
            sb.append("  暂无销售数据\n");
        }

        sb.append("\n【滞销 TOP5 菜品】\n");
        if (bottom5 != null && !bottom5.isEmpty()) {
            for (int i = 0; i < bottom5.size(); i++) {
                GoodsSalesDTO dto = bottom5.get(i);
                sb.append("  ").append(i + 1).append(". ")
                        .append(dto.getName())
                        .append(" - 销量 ").append(dto.getNumber()).append(" 份\n");
            }
        } else {
            sb.append("  暂无销售数据\n");
        }

        return sb.toString();
    }
}
