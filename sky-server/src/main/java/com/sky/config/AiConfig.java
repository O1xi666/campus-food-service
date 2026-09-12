package com.sky.config;

import com.sky.utils.QwenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 大模型配置装载：把 sky.ai.* 注入到 {@link QwenUtil}，业务代码只依赖 QwenUtil.chat()。
 *
 * provider = openai    → OpenAI 兼容协议（DeepSeek 等），端点 {base-url}/chat/completions
 * provider = dashscope → 阿里云百炼原生协议，端点 {base-url}/services/aigc/text-generation/generation
 */
@Configuration
@Slf4j
public class AiConfig {

    @Value("${sky.ai.provider:openai}")
    private String provider;

    @Value("${sky.ai.api-key:}")
    private String apiKey;

    @Value("${sky.ai.base-url:}")
    private String baseUrl;

    @Value("${sky.ai.model:}")
    private String model;

    @PostConstruct
    public void init() {
        QwenUtil.init(provider, apiKey, baseUrl, model);
        log.info("AI 配置加载完成 - provider={}, model={}, baseUrl={}, apiKey={}",
                QwenUtil.getProvider(), QwenUtil.getModel(), baseUrl, mask(apiKey));
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("未检测到 AI API Key，请通过环境变量 SKY_AI_API_KEY 注入，否则 AI 接口不可用");
        }
    }

    private String mask(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "(未配置)";
        }
        String k = key.trim();
        return k.length() <= 10 ? "***" : k.substring(0, 6) + "***" + k.substring(k.length() - 4);
    }
}
