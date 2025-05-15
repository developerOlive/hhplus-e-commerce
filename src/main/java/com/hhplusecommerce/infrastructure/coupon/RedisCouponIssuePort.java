package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;

/**
 * Redis를 활용하여 쿠폰 발급 요청 및 상태를 관리하는 구현체
 */
@Repository
public class RedisCouponIssuePort implements CouponIssuePort {

    private final StringRedisTemplate redisTemplate;
    private final ZSetOperations<String, String> zSetOps;

    public RedisCouponIssuePort(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    /**
     * 쿠폰 발급 여부 조회
     */
    @Override
    public boolean isIssued(String couponIssuedKey, String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(couponIssuedKey, userId));
    }

    /**
     * 쿠폰 발급 요청을 대기열에 등록하고 TTL 설정
     */
    @Override
    public void addToRequestQueue(String couponRequestKey, String userId, long scoreTimestamp) {
        zSetOps.add(couponRequestKey, userId, scoreTimestamp);
        redisTemplate.expire(couponRequestKey, Duration.ofDays(7));
    }

    /**
     * 쿠폰 발급 요청을 batchSize 단위로 꺼냄
     */
    @Override
    public Set<String> popRequests(String couponRequestKey, int batchSize) {
        Set<ZSetOperations.TypedTuple<String>> popped = zSetOps.popMin(couponRequestKey, batchSize);
        if (popped == null) return Set.of();
        return popped.stream().map(ZSetOperations.TypedTuple::getValue).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 쿠폰 재고 감소
     */
    @Override
    public Long decrementStock(String couponStockKey) {
        return redisTemplate.opsForValue().decrement(couponStockKey);
    }

    /**
     * 쿠폰 발급 완료된 사용자 처리
     */
    @Override
    public void addIssuedUser(String couponIssuedKey, String userId) {
        redisTemplate.opsForSet().add(couponIssuedKey, userId);
    }

    /**
     * 재고 복구 (증가)
     */
    public void incrementStock(String couponStockKey) {
        redisTemplate.opsForValue().increment(couponStockKey);
    }

    /**
     * 발급 완료 사용자 집합에서 삭제 (발급 실패시 롤백용)
     */
    public void removeIssuedUser(String couponIssuedKey, String userId) {
        redisTemplate.opsForSet().remove(couponIssuedKey, userId);
    }
}
