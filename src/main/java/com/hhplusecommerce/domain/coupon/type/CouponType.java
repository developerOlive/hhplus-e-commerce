package com.hhplusecommerce.domain.coupon.type;

public enum CouponType {
    LIMITED("수량 제한"),
    UNLIMITED("무제한");

    private final String description;
    CouponType(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
