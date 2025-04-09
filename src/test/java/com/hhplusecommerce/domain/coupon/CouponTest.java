package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {
    private static final String COUPON_NAME = "10% 할인 쿠폰";
    private static final CouponDiscountType DISCOUNT_TYPE = CouponDiscountType.FIXED_RATE;
    private static final BigDecimal DISCOUNT_VALUE = BigDecimal.valueOf(100);
    private static final int MAX_QUANTITY = 100;
    private static final int ISSUED_QUANTITY = 0;
    private static final CouponStatus COUPON_STATUS = CouponStatus.ACTIVE;
    private static final LocalDate VALID_START_DATE = LocalDate.now();
    private static final LocalDate VALID_END_DATE = LocalDate.now().plusDays(30);

    private Coupon coupon;

    @Nested
    class 쿠폰_생성_성공 {

        @Test
        void 정상적으로_쿠폰을_생성한다() {
            coupon = new Coupon(COUPON_NAME, DISCOUNT_TYPE, DISCOUNT_VALUE, MAX_QUANTITY, VALID_START_DATE, VALID_END_DATE);

            assertThat(coupon.getCouponName()).isEqualTo(COUPON_NAME);
            assertThat(coupon.getDiscountType()).isEqualTo(DISCOUNT_TYPE);
            assertThat(coupon.getDiscountValue()).isEqualTo(DISCOUNT_VALUE);
            assertThat(coupon.getMaxQuantity()).isEqualTo(MAX_QUANTITY);
            assertThat(coupon.getIssuedQuantity()).isEqualTo(ISSUED_QUANTITY);
            assertThat(coupon.getCouponStatus()).isEqualTo(COUPON_STATUS);
            assertThat(coupon.getValidStartDate()).isEqualTo(VALID_START_DATE);
            assertThat(coupon.getValidEndDate()).isEqualTo(VALID_END_DATE);
        }

        @Test
        void 할인_값이_0보다_작으면_예외가_발생한다() {
            assertThatThrownBy(() -> new Coupon(COUPON_NAME, DISCOUNT_TYPE, BigDecimal.valueOf(-1), MAX_QUANTITY, VALID_START_DATE, VALID_END_DATE))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_COUPON_VALUE.getMessage());
        }
    }
}
