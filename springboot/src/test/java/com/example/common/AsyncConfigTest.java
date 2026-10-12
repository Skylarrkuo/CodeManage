package com.example.common;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 守着评测线程池的参数。
 *
 * <p>之前项目没有线程池配置，走的是 Spring 默认的 {@code SimpleAsyncTaskExecutor}
 * （每个任务新建线程、无上限）。这些参数是刻意选的，改动会被这里拦下。</p>
 */
class AsyncConfigTest {

    @Test
    void judgeExecutorIsBoundedAndDoesNotSilentlyDropTasks() {
        ThreadPoolTaskExecutor executor = new AsyncConfig().judgeExecutor();
        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(4);
            assertThat(executor.getMaxPoolSize()).isEqualTo(8);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("judge-");

            ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
            assertThat(pool.getQueue().remainingCapacity()).isEqualTo(200);
            // 队列满时由调用线程直接执行：宁可慢，也不能让提交永远停在 PENDING
            assertThat(pool.getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
        } finally {
            executor.shutdown();
        }
    }
}
