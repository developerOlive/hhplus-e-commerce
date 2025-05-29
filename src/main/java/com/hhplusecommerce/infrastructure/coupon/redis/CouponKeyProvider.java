package com.hhplusecommerce.infrastructure.coupon.redis;

public interface CouponKeyProvider {
    String requestKey(Long couponId);
    String issuedKey(Long couponId);
    String stockKey(Long couponId);
}
