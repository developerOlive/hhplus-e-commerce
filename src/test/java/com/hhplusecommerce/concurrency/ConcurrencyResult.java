package com.hhplusecommerce.concurrency;

public class ConcurrencyResult {
    private final int successCount;
    private final int errorCount;

    public ConcurrencyResult(int successCount, int errorCount) {
        this.successCount = successCount;
        this.errorCount = errorCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }
}
