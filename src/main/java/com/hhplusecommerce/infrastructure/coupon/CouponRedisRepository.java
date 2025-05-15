package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

import static java.lang.Boolean.TRUE;

/**
 * Redis 기반으로 쿠폰 발급 요청, 중복 발급 체킹, 재고 관리를 수행
 */
@Repository
public class CouponRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final ZSetOperations<String, String> zSetOps;

    public CouponRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    private static final Long REQUEST_QUEUE_TTL = 3L;

    /** 중복 발급 여부 체크 */
    public boolean isIssued(String couponIssuedKey, String userId) {
        return redisTemplate.opsForSet().isMember(couponIssuedKey, userId);
    }

    /** 사용자 쿠폰 발급 대기열에 등록 */
    public void addToRequestQueue(String couponRequestKey, String userId, long scoreTimestamp) {
        Boolean isMember = redisTemplate.opsForZSet().score(couponRequestKey, userId) != null;
        if (TRUE.equals(isMember)) {
            throw new CustomException(ErrorType.COUPON_ALREADY_ISSUED);
        }

        zSetOps.add(couponRequestKey, userId, scoreTimestamp);
        redisTemplate.expire(couponRequestKey, Duration.ofDays(REQUEST_QUEUE_TTL));
    }
}
