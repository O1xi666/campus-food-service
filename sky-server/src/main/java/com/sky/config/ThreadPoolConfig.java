package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 开启异步任务注解
public class ThreadPoolConfig {

    @Bean("taskExecutor") // 给这个线程池起个名字叫 taskExecutor
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心线程数 (电脑默认工作的工人数量)
        // 笔记本通常双核或四核，我们设为核心数+1，防止刚好卡满
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() + 1);

        // 2. 最大线程数 (任务太多时，最多能找多少个临时工)
        // 笔记本不要开太多，防止卡死，设为 核心数 * 2
        executor.setMaxPoolSize((Runtime.getRuntime().availableProcessors() + 1) * 2);

        // 3. 队列容量 (车间外面排队的板凳数量)
        // 如果线程都在忙，新任务就在队列里排队。设个100够用了。
        executor.setQueueCapacity(100);

        // 4. 线程名字前缀 (方便看日志用的)
        executor.setThreadNamePrefix("Laptop-Task-");

        // 5. 线程存活时间 (临时工空闲多久后解散)
        executor.setKeepAliveSeconds(60);

        // 6. 拒绝策略 (如果队列满了，新任务怎么办)
        // CallerRunsPolicy：如果满了，就由提交任务的人（你的主线程）自己执行，防止系统崩溃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 7. 初始化
        executor.initialize();

        return executor;
    }
}