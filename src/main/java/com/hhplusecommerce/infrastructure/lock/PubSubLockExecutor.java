package com.hhplusecommerce.infrastructure.lock;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import com.hhplusecommerce.support.lock.LockExecutor;
import com.hhplusecommerce.support.lock.LockType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubLockExecutor implements LockExecutor {

    private final RedissonClient redissonClient;

    @Override
    public LockType getType() {
        return LockType.PUBSUB;
    }

    @Override
    public <T> T execute(String key, long waitTime, long leaseTime, Supplier<T> action) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[LOCK] 락 획득 실패: {}", key);
                throw new CustomException(ErrorType.LOCK_ACQUISITION_FAILED);
            }

            log.info("[LOCK] 락 획득 성공: {}", key);
            return action.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorType.LOCK_ACQUISITION_FAILED);
        } catch (Exception e) {
            throw new RuntimeException("락 수행 중 예외 발생", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    log.info("[LOCK] 락 해제 성공: {}", key);
                } catch (Exception e) {
                    log.error("락 해제 실패: {}", key, e);
                }
            }
        }
    }
}
