package com.hhplusecommerce.domain.coupon.type;

public enum CouponIssueStatus {
    READY,        // 발급 대기 중
    PROCESSING,   // 발급 처리 중
    FINISHED,     // 발급 완료 (재고 소진)
    EXPIRED,      // 발급 기간 만료
    CANCELED      // 발급 취소
}
