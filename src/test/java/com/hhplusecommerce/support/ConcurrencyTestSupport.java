package com.hhplusecommerce.support;

import com.hhplusecommerce.concurrency.ConcurrencyResult;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 동시성 테스트용 서포트 클래스
 */
@Slf4j
public abstract class ConcurrencyTestSupport extends IntegrationTestSupport {

    @Autowired
    private DbCleaner dbCleaner;

    @AfterEach
    void tearDown() {
        dbCleaner.execute();
        log.info("테스트 후 DB 초기화 완료");
    }

    /**
     * 동일한 Runnable을 여러 쓰레드로 동시에 실행하고 성공/실패 반환
     */
    protected ConcurrencyResult executeConcurrency(int threadCount, Runnable runnable) {
        List<Runnable> runnables = IntStream.range(0, threadCount)
                .mapToObj(i -> runnable)
                .toList();
        return executeConcurrency(runnables);
    }

    /**
     * 주어진 Runnable 리스트를 각각 병렬로 실행하고 성공/실패 반환
     */
    protected ConcurrencyResult executeConcurrency(List<Runnable> runnables) {
        ExecutorService executorService = Executors.newFixedThreadPool(runnables.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        for (Runnable runnable : runnables) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    runnable.run();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("스레드 실행 중 오류 발생: {}", e.getMessage(), e);
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        return new ConcurrencyResult(successCount.get(), errorCount.get());
    }
}
