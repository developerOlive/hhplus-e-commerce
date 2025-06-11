package com.hhplusecommerce.infrastructure.coupon.redis;

import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

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
        Boolean added = zSetOps.add(couponRequestKey, userId, scoreTimestamp);
        if (Boolean.FALSE.equals(added)) {
            throw new CustomException(ErrorType.COUPON_ALREADY_ISSUED);
        }

        redisTemplate.expire(couponRequestKey, REQUEST_QUEUE_TTL);
    }
}
