package com.hhplusecommerce.support.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class DefaultLockTemplate implements LockTemplate {

    @Override
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit, LockCallback<T> callback) throws Throwable {
        acquireLock(key, waitTime, leaseTime, timeUnit);

        try {
            T result = callback.doInLock();

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        releaseLock(key);
                    }
                });
            } else {
                releaseLock(key);
            }

            return result;
        } catch (Throwable t) {
            releaseLock(key);
            throw t;
        }
    }

    public abstract void acquireLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    public abstract void releaseLock(String key);
}
