package com.hhplusecommerce.domain.coupon.type;

public enum CouponStatus {
    ACTIVE("사용 가능"),
    INACTIVE("사용 불가");

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
