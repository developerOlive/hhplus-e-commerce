package com.hhplusecommerce.support.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redisson 분산 락을 위한 애노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    // SpEL 표현식을 사용하여 락의 키를 동적으로 생성
    String value();

    // 락 유형
    LockType lockType() default LockType.PUBSUB;

    // 락 대기 시간
    long waitTime() default 2L;

    // 락 임대 시간
    long leaseTime() default 4L;
}
