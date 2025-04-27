package com.hhplusecommerce;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     * 동일한 Runnable을 여러 쓰레드로 동시에 실행
     */
    protected void executeConcurrency(int threadCount, Runnable runnable) {
        List<Runnable> runnables = IntStream.range(0, threadCount)
                .mapToObj(i -> runnable)
                .toList();
        executeConcurrency(runnables);
    }

    /**
     * 주어진 Runnable 리스트를 각각 병렬로 실행
     */
    protected void executeConcurrency(List<Runnable> runnables) {
        ExecutorService executorService = Executors.newFixedThreadPool(runnables.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Runnable runnable : runnables) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.error("스레드 실행 중 오류 발생: {}", e.getMessage());
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }
}
