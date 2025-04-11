package com.hhplusecommerce.domain.coupon;

public enum CouponDiscountType {
    FIXED_AMOUNT("정액 할인"),
    FIXED_RATE("비율 할인");

    private final String description;

    CouponDiscountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
