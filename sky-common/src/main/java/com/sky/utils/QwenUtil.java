package com.sky.utils;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class QwenUtil {

    // 1. 去掉 final 和 初始值，变成一个可设置的静态变量
    private static String API_KEY = "";

    // 2. 提供一个静态方法，让 Spring 启动时把配置里的 Key 传进来
    public static void setApiKey(String key) {
        API_KEY = key;
    }

    private static final String URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public static String chat(String prompt) {
        // 3. 增加校验
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            return "系统错误：AI API Key 未配置，请联系管理员";
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-plus");
        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);

        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.put(userMsg);

        JSONObject input = new JSONObject();
        input.put("messages", messages);
        requestBody.put("input", input);

        RequestBody bodyContent = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer " + API_KEY) // 这里使用动态变量
                .addHeader("Content-Type", "application/json")
                .post(bodyContent)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "AI 服务调用失败: " + response.message();
            }
            String respBody = response.body().string();
            JSONObject json = new JSONObject(respBody);


            if (json.has("output")) {
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
            }
            return "未获取到有效回复";

        } catch (IOException e) {
            e.printStackTrace();
            return "网络异常：" + e.getMessage();
        }
    }
}