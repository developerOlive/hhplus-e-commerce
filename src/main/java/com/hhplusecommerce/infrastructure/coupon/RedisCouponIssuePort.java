package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

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
}
