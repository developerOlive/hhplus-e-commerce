package com.hhplusecommerce.support;

import com.hhplusecommerce.concurrency.ConcurrencyResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public abstract class ConcurrencyTestSupport extends IntegrationTestSupport {

    @Autowired
    private DbCleaner dbCleaner;

    @AfterEach
    void tearDown() {
        dbCleaner.execute();
        log.info("테스트 후 DB 초기화 완료");
    }

    protected ConcurrencyResult executeConcurrency(int threadCount, Runnable runnable) {
        return executeConcurrency(IntStream.range(0, threadCount)
                .mapToObj(i -> runnable)
                .toList());
    }

    protected ConcurrencyResult executeConcurrency(List<Runnable> tasks) {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        for (Runnable task : tasks) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    task.run();
                    success.incrementAndGet();
                } catch (Exception e) {
                    failure.incrementAndGet();
                    log.error("실행 실패: {}", e.getMessage(), e);
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        return new ConcurrencyResult(success.get(), failure.get());
    }
}
