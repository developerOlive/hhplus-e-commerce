package com.hhplusecommerce.support.lock;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final LockStrategyFactory lockStrategyFactory;
    private final LockKeyGenerator keyGenerator;

    @Around("@annotation(com.hhplusecommerce.support.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);

        String key = keyGenerator.generateKey(method, joinPoint.getArgs(), method.getName(), annotation.value());
        LockExecutor executor = lockStrategyFactory.getExecutor(annotation.lockType());

        try {
            return executor.execute(
                    key,
                    annotation.waitTime(),
                    annotation.leaseTime(),
                    () -> {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable t) {
                            if (t instanceof CustomException) {
                                throw (CustomException) t;
                            }
                            throw new CustomException(ErrorType.LOCK_ACQUISITION_FAILED);
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Lock acquisition failed for key: {}", key, e);
            throw new CustomException(ErrorType.LOCK_ACQUISITION_FAILED);
        }
    }
}
