package com.hhplusecommerce.domain.coupon.type;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum CouponDiscountType {

    FIXED_AMOUNT("정액할인") {
        @Override
        public BigDecimal discount(BigDecimal totalAmount, BigDecimal value) {
            if (totalAmount == null || value == null) return BigDecimal.ZERO;
            return value.min(totalAmount);
        }
    },

    FIXED_RATE("정률할인") {
        @Override
        public BigDecimal discount(BigDecimal totalAmount, BigDecimal value) {
            if (totalAmount == null || value == null) return BigDecimal.ZERO;
            return totalAmount
                    .multiply(value)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
        }
    };

    private final String description;

    CouponDiscountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract BigDecimal discount(BigDecimal totalAmount, BigDecimal value);
}
