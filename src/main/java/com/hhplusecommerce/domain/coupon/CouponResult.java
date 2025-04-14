package com.hhplusecommerce.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponResult(
        Long couponId,
        String couponName,
        CouponDiscountType discountType,
        BigDecimal discountValue,
        CouponStatus couponStatus,
        CouponUsageStatus couponUsageStatus,
        LocalDate validStartDate,
        LocalDate validEndDate
) {
    public static CouponResult from(CouponHistory couponHistory, Coupon coupon) {
        return new CouponResult(
                coupon.getId(),
                coupon.getCouponName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getCouponStatus(),
                couponHistory.getCouponUsageStatus(),
                coupon.getValidStartDate(),
                coupon.getValidEndDate()
        );
    }
}
