package com.hhplusecommerce.support.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Spring @Async 비동기 처리 기능에 사용되는 설정 클래스입니다.
 *
 * 주요 기능:
 * - CPU 코어 수 및 JVM 메모리에 따라 스레드 풀 크기/큐 용량 자동 조절
 * - 스레드 이름 접두사 지정
 * - 비동기 예외 발생 시 로깅 처리
 */
@Configuration
@Profile("!test")
public class AsyncConfiguration implements AsyncConfigurer {

    // 메모리 기준 (단위: MB)
    private static final int LOW_MEMORY_THRESHOLD = 1024;
    private static final int MEDIUM_MEMORY_THRESHOLD = 4096;

    // 큐 용량 설정
    private static final int LOW_MEMORY_QUEUE_CAPACITY = 200;
    private static final int MEDIUM_MEMORY_QUEUE_CAPACITY = 500;
    private static final int HIGH_MEMORY_QUEUE_CAPACITY = 1000;

    // 스레드 접두사
    private static final String THREAD_NAME_PREFIX = "Async-Executor-";

    @Bean("asyncExecutor")
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int processors = Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(processors * 2);
        executor.setMaxPoolSize(processors * 4);
        executor.setQueueCapacity(resolveQueueCapacity());
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.initialize();

        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncExceptionHandler();
    }

    private int resolveQueueCapacity() {
        int maxMemoryMB = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));

        if (maxMemoryMB <= LOW_MEMORY_THRESHOLD) {
            return LOW_MEMORY_QUEUE_CAPACITY;
        }
        if (maxMemoryMB <= MEDIUM_MEMORY_THRESHOLD) {
            return MEDIUM_MEMORY_QUEUE_CAPACITY;
        }
        return HIGH_MEMORY_QUEUE_CAPACITY;
    }

    @Slf4j
    public static class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("비동기 메서드 예외 발생 - 메서드: {}, 메시지: {}", method.getName(), ex.getMessage(), ex);
        }
    }
}
