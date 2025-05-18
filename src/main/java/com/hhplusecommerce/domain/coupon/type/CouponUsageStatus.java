package com.hhplusecommerce.domain.coupon.type;

public enum CouponUsageStatus {
    AVAILABLE("사용 가능"),
    USED("사용 완료"),
    EXPIRED("만료됨");

    private final String description;

    CouponUsageStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
