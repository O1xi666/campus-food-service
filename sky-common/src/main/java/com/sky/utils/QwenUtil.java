package com.sky.utils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 大模型调用工具类。
 *
 * 支持两种协议，通过配置切换，业务代码无需改动：
 *  1. openai    —— OpenAI 兼容协议（POST {base-url}/chat/completions），DeepSeek 等厂商适用
 *  2. dashscope —— 阿里云百炼原生协议（POST {base-url}/services/aigc/text-generation/generation）
 *
 * 配置项见 application.yml 的 sky.ai.*；API Key 通过环境变量注入，不写入仓库。
 */
public class QwenUtil {

    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_DASHSCOPE = "dashscope";

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";

    private static volatile String provider = PROVIDER_OPENAI;
    private static volatile String apiKey = "";
    private static volatile String baseUrl = DEFAULT_OPENAI_BASE_URL;
    private static volatile String model = "deepseek-chat";

    private QwenUtil() {
    }

    /**
     * Spring 启动时注入配置
     */
    public static void init(String provider, String apiKey, String baseUrl, String model) {
        String p = (provider == null || provider.trim().isEmpty())
                ? PROVIDER_OPENAI
                : provider.trim().toLowerCase();
        QwenUtil.provider = p;
        QwenUtil.apiKey = apiKey == null ? "" : apiKey.trim();

        String url = baseUrl == null ? "" : baseUrl.trim();
        if (url.isEmpty()) {
            url = PROVIDER_DASHSCOPE.equals(p) ? DEFAULT_DASHSCOPE_BASE_URL : DEFAULT_OPENAI_BASE_URL;
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        QwenUtil.baseUrl = url;
        QwenUtil.model = (model == null || model.trim().isEmpty())
                ? (PROVIDER_DASHSCOPE.equals(p) ? "qwen-plus" : "deepseek-chat")
                : model.trim();
    }

    /**
     * 仅设置 Key（兼容旧用法）
     */
    public static void setApiKey(String key) {
        apiKey = key == null ? "" : key.trim();
    }

    public static String getProvider() {
        return provider;
    }

    public static String getModel() {
        return model;
    }

    public static String chat(String prompt) {
        if (apiKey.isEmpty()) {
            return "系统错误：AI API Key 未配置，请通过环境变量 SKY_AI_API_KEY 注入";
        }

        boolean dashscope = PROVIDER_DASHSCOPE.equals(provider);
        String url = dashscope
                ? baseUrl + "/services/aigc/text-generation/generation"
                : baseUrl + "/chat/completions";
        String requestJson = dashscope ? dashScopeBody(prompt) : openAiBody(prompt);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                return "AI 服务调用失败: HTTP " + response.code() + " " + respBody;
            }
            String content = dashscope ? parseDashScope(respBody) : parseOpenAiCompatible(respBody);
            return content == null ? "未获取到有效回复" : content;
        } catch (IOException e) {
            return "网络异常：" + e.getMessage();
        }
    }

    /**
     * OpenAI 兼容协议请求体
     */
    private static String openAiBody(String prompt) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.put(userMsg);
        body.put("messages", messages);
        return body.toString();
    }

    /**
     * 阿里云百炼原生协议请求体
     */
    private static String dashScopeBody(String prompt) {
        JSONObject body = new JSONObject();
        body.put("model", model);

        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        body.put("parameters", parameters);

        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.put(userMsg);

        JSONObject input = new JSONObject();
        input.put("messages", messages);
        body.put("input", input);
        return body.toString();
    }

    private static String parseOpenAiCompatible(String respBody) {
        JSONObject json = new JSONObject(respBody);
        if (!json.has("choices")) {
            return null;
        }
        JSONArray choices = json.getJSONArray("choices");
        if (choices.length() == 0) {
            return null;
        }
        JSONObject choice = choices.getJSONObject(0);
        if (!choice.has("message")) {
            return null;
        }
        JSONObject message = choice.getJSONObject("message");
        return message.has("content") ? message.getString("content") : null;
    }

    private static String parseDashScope(String respBody) {
        JSONObject json = new JSONObject(respBody);
        if (!json.has("output")) {
            return null;
        }
        JSONObject output = json.getJSONObject("output");
        if (output.has("choices")) {
            JSONArray choices = output.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                if (choice.has("message")) {
                    JSONObject message = choice.getJSONObject("message");
                    if (message.has("content")) {
                        return message.getString("content");
                    }
                }
            }
        }
        return output.has("text") ? output.getString("text") : null;
    }
}
