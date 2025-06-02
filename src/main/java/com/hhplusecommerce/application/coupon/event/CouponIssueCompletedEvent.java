package com.hhplusecommerce.application.coupon.event;

public record CouponIssueCompletedEvent(
        Long userId,
        Long couponId,
        CouponIssueResult result,
        String message
) {}
