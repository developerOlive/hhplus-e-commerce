package com.hhplusecommerce.domain.coupon.port;

public interface CouponIssuePort {

    boolean isIssued(String couponIssuedKey, String userId);

    void addToRequestQueue(String couponRequestKey, String userId, long scoreTimestamp);
}
