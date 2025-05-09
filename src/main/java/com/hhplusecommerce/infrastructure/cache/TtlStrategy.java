package com.hhplusecommerce.infrastructure.cache;

import java.time.Duration;

/**
 * TTL 계산 전략 인터페이스
 */
public interface TtlStrategy {
    Duration computeTTL(Object keyParams);
}
