package com.hhplusecommerce.support.lock;

import java.util.function.Supplier;

/**
 * 락 획득 및 비즈니스 로직 실행을 추상화한 함수형 인터페이스
 */
public interface LockExecutor {
    LockType getType();
    <T> T execute(String key, long waitTime, long leaseTime, Supplier<T> action);
}
