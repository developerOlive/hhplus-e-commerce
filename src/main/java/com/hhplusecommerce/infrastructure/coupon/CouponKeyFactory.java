package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.application.coupon.CouponKeyProvider;
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

    @Override
    public String stockKey(Long couponId) {
        return PREFIX + ":stock:" + couponId;
    }
}
