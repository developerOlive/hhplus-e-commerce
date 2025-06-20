package com.hhplusecommerce.infrastructure.coupon.redis;

import org.springframework.stereotype.Component;

@Component
public class CouponKeyFactory implements CouponKeyProvider {

    private static final String PREFIX = "coupon";

    @Override
    public String requestKey(Long couponId) {
        return PREFIX + ":request:" + couponId;
    }

    @Override
    public String issuedKey(Long couponId) {
        return PREFIX + ":issued:" + couponId;
    }
}
