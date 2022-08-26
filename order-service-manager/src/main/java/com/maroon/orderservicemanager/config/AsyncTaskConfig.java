package com.maroon.orderservicemanager.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池 防止线程爆炸 系统启动会自动生成一个线程池
 */
@Configuration
@EnableAsync
public class AsyncTaskConfig implements AsyncConfigurer {
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    /**
     * 创建一个异步线程池
     * @return 线程池
     */
    @Override
    @Bean
    public Executor getAsyncExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor =new ThreadPoolTaskExecutor();
        //设置核心线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        //最大线程数 当 CorePoolSize<线程数<MaxPoolSize 会创建新的线程处理任务 当MaxPoolSize<=线程数 会报异常 只有在超过核心线程数且线程队列满了之后才会启动新的线程
        threadPoolTaskExecutor.setMaxPoolSize(100);
        //线程队列容量 当线程数大于核心线程数CorePoolSize 多的线程会在队列中等待
        threadPoolTaskExecutor.setQueueCapacity(10);
        //设置线程池关闭的时候等待所有任务执行完才关闭
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        //该方法用来设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
        //线程名
        threadPoolTaskExecutor.setThreadNamePrefix("Rabbit-Async-");
        //初始化线程
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
