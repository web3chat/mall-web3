package com.fzm.mall.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration
@EnableScheduling
public class ScheduledConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 基础配置
        scheduler.setThreadNamePrefix("mc-scheduling-");
        scheduler.setPoolSize(50); // 设置核心线程池大小
        scheduler.setAwaitTerminationSeconds(180); // 设置等待任务完成的最大超时时间（秒），防止无限期阻塞
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 应用关闭时，等待所有定时任务执行完成

        scheduler.initialize();

        return scheduler;
    }
}
