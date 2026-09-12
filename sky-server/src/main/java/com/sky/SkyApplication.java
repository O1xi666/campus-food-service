package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableTransactionManagement //寮€鍚敞瑙ｆ柟寮忕殑浜嬪姟绠＄悊
@Slf4j
@EnableCaching
@EnableAsync
@EnableScheduling
public class SkyApplication {
    public static void main(String[] args) {
        // 鍚姩椤圭洰骞惰幏鍙栦笂涓嬫枃锛堟牳蹇冧慨鏀癸細鎶婂師鏉ョ殑涓€琛屾媶鎴愪袱琛岋紝鑾峰彇涓婁笅鏂囧璞★級
        ConfigurableApplicationContext context = SpringApplication.run(SkyApplication.class, args);
        // 鑾峰彇鐜閰嶇疆瀵硅薄锛岀敤鏉ヨ鍙杫ml閲岀殑閰嶇疆
        Environment env = context.getEnvironment();

        // 打印项目实际读取的数据库配置（关键：用log.info打印，和你的日志风格一致）
        log.info("===== 项目实际读取的数据库配置 =====");
        log.info("数据库名: {}", env.getProperty("sky.datasource.database"));
        log.info("用户名: {}", env.getProperty("sky.datasource.username"));
        log.info("密码: {}", env.getProperty("sky.datasource.password"));
        log.info("主机: {}", env.getProperty("sky.datasource.host"));
        log.info("端口: {}", env.getProperty("sky.datasource.port"));
        log.info("==================================");

        // 保留你原来的日志
        log.info("server started");
    }

}

