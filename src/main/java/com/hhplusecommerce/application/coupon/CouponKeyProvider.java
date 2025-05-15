package com.hhplusecommerce.application.coupon;

public interface CouponKeyProvider {
    String requestKey(Long couponId);
    String issuedKey(Long couponId);
    String stockKey(Long couponId);
}
