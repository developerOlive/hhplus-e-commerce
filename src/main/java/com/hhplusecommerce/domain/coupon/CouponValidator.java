package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;

public class CouponValidator {

    public static void validateUsableCoupon(Coupon coupon, CouponHistory history) {
        if (!coupon.isAvailable()) {
            throw new CustomException(ErrorType.COUPON_INACTIVE);
        }

        if (!history.isAvailable()) {
            throw new CustomException(ErrorType.COUPON_ALREADY_USED);
        }
    }
}
