package com.hhplusecommerce.infrastructure.lock;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import com.hhplusecommerce.support.lock.LockExecutor;
import com.hhplusecommerce.support.lock.LockType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpinLockExecutor implements LockExecutor {

    private final StringRedisTemplate redisTemplate;
    private static final long DEFAULT_TIMEOUT_MILLIS = 3000;
    private static final long RETRY_DELAY_MILLIS = 100;

    @Override
    public LockType getType() {
        return LockType.SPINNING;
    }

    @Override
    public <T> T execute(String key, long waitTime, long leaseTime, Supplier<T> action) {
        String value = UUID.randomUUID().toString();
        long end = System.currentTimeMillis() + DEFAULT_TIMEOUT_MILLIS;

        boolean acquired = false;
        try {
            while (System.currentTimeMillis() < end) {
                Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(leaseTime));
                if (Boolean.TRUE.equals(success)) {
                    acquired = true;
                    log.info("[LOCK] SpinLock 획득 성공: {}", key);
                    return action.get();
                }

                Thread.sleep(RETRY_DELAY_MILLIS);
            }

            throw new CustomException(ErrorType.LOCK_ACQUISITION_FAILED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorType.LOCK_ACQUISITION_FAILED);

        } finally {
            if (acquired) {
                String currentValue = redisTemplate.opsForValue().get(key);
                if (value.equals(currentValue)) {
                    redisTemplate.delete(key);
                    log.info("[LOCK] SpinLock 해제 완료: {}", key);
                }
            }
        }
    }
}
