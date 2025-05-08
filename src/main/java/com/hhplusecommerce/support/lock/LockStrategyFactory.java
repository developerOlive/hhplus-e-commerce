package com.hhplusecommerce.support.lock;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * LockType 기반으로 LockExecutor를 제공하는 팩토리
 */
@Component
public class LockStrategyFactory {

    private final Map<LockType, LockExecutor> lockExecutorMap;

    public LockStrategyFactory(List<LockExecutor> executors) {
        this.lockExecutorMap = executors.stream()
                .collect(Collectors.toMap(LockExecutor::getType, Function.identity()));
    }

    public LockExecutor getExecutor(LockType lockType) {
        LockExecutor executor = lockExecutorMap.get(lockType);
        if (executor == null) {
            throw new IllegalStateException("등록되지 않은 LockType 입니다. : " + lockType);
        }

        return executor;
    }
}
