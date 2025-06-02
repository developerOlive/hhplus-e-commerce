package com.hhplusecommerce.infrastructure.coupon.redis;

import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

/**
 * Redis를 활용하여 쿠폰 발급 요청, 중복 발급 체킹, 재고 관리, 키 생성 등을 수행하는 구현체
 */
@Repository
public class RedisCouponIssueAdapter implements CouponIssuePort {

    private final StringRedisTemplate redisTemplate;
    private final ZSetOperations<String, String> zSetOps;

    private static final Duration REQUEST_QUEUE_TTL = Duration.ofDays(7);

    public RedisCouponIssueAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    /**
     * 쿠폰 발급 여부 조회
     */
    @Override
    public boolean isIssued(String couponIssuedKey, String userId) {
        return TRUE.equals(redisTemplate.opsForSet().isMember(couponIssuedKey, userId));
    }

    /**
     * 사용자 쿠폰 발급 대기열에 등록
     */
    @Override
    public void addToRequestQueue(String couponRequestKey, String userId, long scoreTimestamp) {
        // 중복 요청 방지 로직
        Boolean existsInQueue = redisTemplate.opsForZSet().score(couponRequestKey, userId) != null;
        if (TRUE.equals(existsInQueue)) {
            // 이미 대기열에 존재하면 재요청으로 간주
            throw new CustomException(ErrorType.COUPON_ALREADY_ISSUED);
        }

        zSetOps.add(couponRequestKey, userId, scoreTimestamp);
        redisTemplate.expire(couponRequestKey, REQUEST_QUEUE_TTL);
    }

    /**
     * Sorted Set에서 발급 요청을 배치 단위로 꺼냄
     */
    @Override
    public Set<String> popRequests(String couponRequestKey, int batchSize) {
        Set<ZSetOperations.TypedTuple<String>> popped = zSetOps.popMin(couponRequestKey, batchSize);
        if (popped == null || popped.isEmpty()) {
            return Set.of();
        }
        return popped.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * 쿠폰 재고 수량 감소
     */
    @Override
    public Long decrementStock(String couponStockKey) {
        return redisTemplate.opsForValue().decrement(couponStockKey);
    }

    /**
     * Redis Set에 쿠폰 발급 완료 사용자 추가
     */
    @Override
    public void addIssuedUser(String couponIssuedKey, String userId) {
        redisTemplate.opsForSet().add(couponIssuedKey, userId);
    }

    /**
     * 재고 복구 (증가)
     */
    @Override
    public void incrementStock(String couponStockKey) {
        redisTemplate.opsForValue().increment(couponStockKey);
    }

    /**
     * 발급 완료 사용자 집합에서 삭제 (발급 실패 시 롤백용)
     */
    @Override // Port에 추가된 메서드이므로 @Override
    public void removeIssuedUser(String couponIssuedKey, String userId) {
        redisTemplate.opsForSet().remove(couponIssuedKey, userId);
    }

    @Override
    public String getRequestQueueKey(Long couponId) {
        return "coupon:" + couponId + ":request_queue";
    }

    @Override
    public String getIssuedKey(Long couponId) {
        return "coupon:" + couponId + ":issued_users";
    }

    @Override
    public String getStockKey(Long couponId) {
        return "coupon:" + couponId + ":stock";
    }
}
