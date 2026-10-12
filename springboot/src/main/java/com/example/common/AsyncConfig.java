package com.example.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置。
 *
 * <p>项目的异步任务只有「代码评测」一种（{@code SubmissionService#evaluateAsync}）。
 * 此前未配置任何线程池，走的是 Spring 默认的 {@code SimpleAsyncTaskExecutor} ——
 * 每个提交新建一个线程且没有上限，高并发提交会打爆线程。</p>
 *
 * <p>这里改用有界线程池，参数取保守值，避免打爆后端的 Judge0 实例
 * （每个提交要串行调用 Judge0 N 次，N 为测试点数）。</p>
 */
@Configuration
public class AsyncConfig {

    /** 执行代码评测的线程池 Bean 名称，被 {@code @Async("judgeExecutor")} 引用 */
    public static final String JUDGE_EXECUTOR = "judgeExecutor";

    @Bean(JUDGE_EXECUTOR)
    public ThreadPoolTaskExecutor judgeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("judge-");
        // 队列满时由提交请求的线程直接执行：会让 /submit 短暂变慢，但不会丢任务。
        // 若换成 AbortPolicy，被拒的提交会永远停在 PENDING（静默失败），更糟。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
