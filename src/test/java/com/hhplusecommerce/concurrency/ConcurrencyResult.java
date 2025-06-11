package com.hhplusecommerce.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyResult {
    private final AtomicInteger successCount;
    private final AtomicInteger errorCount;

    public ConcurrencyResult() {
        this.successCount = new AtomicInteger(0);
        this.errorCount = new AtomicInteger(0);
    }

    public void success() {
        successCount.incrementAndGet();
    }

    public void error() {
        errorCount.incrementAndGet();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getErrorCount() {
        return errorCount.get();
    }
}
