package com.fzm.mall.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;


@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor chainEventInsideExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        threadPool.setCorePoolSize(100);
        // 设置最大线程数
        threadPool.setMaxPoolSize(200);
        // 线程池所使用的缓冲队列
        threadPool.setQueueCapacity(10000);
        // 设置线程活跃时间（秒）
        threadPool.setKeepAliveSeconds(60);
        // 等待所有任务结束后再关闭线程池
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        // 线程名称前缀
        threadPool.setThreadNamePrefix("chain-event-inside-");
        // 设置拒绝策略rejection-policy：
        // 当pool已经达到max size的时候，如何处理新任务 CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return threadPool;

    }
}
