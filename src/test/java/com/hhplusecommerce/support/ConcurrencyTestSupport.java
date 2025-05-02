package com.hhplusecommerce.support;

import com.hhplusecommerce.concurrency.ConcurrencyResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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

    protected ConcurrencyResult executeWithLatch(int threadCount, Consumer<ConcurrencyResult> task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrencyResult result = new ConcurrencyResult();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    task.accept(result);
                } catch (Exception e) {
                    result.error();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        return result;
    }
}
