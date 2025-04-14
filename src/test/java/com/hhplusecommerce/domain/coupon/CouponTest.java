package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.hhplusecommerce.domain.coupon.CouponDiscountType.FIXED_AMOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    private static final String NAME = "테스트 쿠폰";
    private static final int MAX_QUANTITY = 3;
    private static final LocalDate START = LocalDate.now().minusDays(1);
    private static final LocalDate END = LocalDate.now().plusDays(1);

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .couponName(NAME)
                .discountType(FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(500))
                .maxQuantity(MAX_QUANTITY)
                .validStartDate(START)
                .validEndDate(END)
                .issuedQuantity(0)
                .couponType(CouponType.LIMITED)
                .build();
    }

    @Nested
    class 할인금액_유효성검사 {

        @Test
        void 할인금액이_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(null)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .couponType(CouponType.LIMITED)
                    .build())
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_COUPON_VALUE.getMessage());
        }

        @Test
        void 할인금액이_0이하면_예외가_발생한다() {
            assertThatThrownBy(() -> Coupon.builder()
                    .couponName(NAME)
                    .discountType(CouponDiscountType.FIXED_RATE)
                    .discountValue(BigDecimal.ZERO)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .couponType(CouponType.LIMITED)
                    .build())
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 쿠폰_유효성 {

        private static final BigDecimal DISCOUNT_PERCENT_10 = BigDecimal.valueOf(10);
        private static final LocalDate EXPIRED_START = LocalDate.now().minusDays(5);
        private static final LocalDate EXPIRED_END = LocalDate.now().minusDays(1);

        @Test
        void 활성상태이고_유효기간_내인_쿠폰은_사용_가능하다() {
            Coupon validCoupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(CouponDiscountType.FIXED_RATE)
                    .discountValue(DISCOUNT_PERCENT_10)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .couponType(CouponType.LIMITED)
                    .build();

            assertThat(validCoupon.isAvailable()).isTrue();
        }

        @Test
        void 유효기간이_지난_쿠폰은_사용_불가능하다() {
            Coupon expiredCoupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(CouponDiscountType.FIXED_RATE)
                    .discountValue(DISCOUNT_PERCENT_10)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(EXPIRED_START)
                    .validEndDate(EXPIRED_END)
                    .issuedQuantity(0)
                    .couponType(CouponType.LIMITED)
                    .build();

            assertThat(expiredCoupon.isAvailable()).isFalse();
        }
    }

    @Nested
    class 발급수량_증가 {

        private static final BigDecimal DISCOUNT_VALUE_500 = BigDecimal.valueOf(500);
        private static final int MAX_QUANTITY_LIMIT = 1;

        @Test
        void 발급수량이_정상적으로_1_증가한다() {
            Coupon coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(DISCOUNT_VALUE_500)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .couponType(CouponType.LIMITED)
                    .build();

            coupon.increaseIssuedQuantity();

            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        }

        @Test
        void 발급수량이_최대치를_초과하면_예외가_발생한다() {
            Coupon coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(DISCOUNT_VALUE_500)
                    .maxQuantity(MAX_QUANTITY_LIMIT)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .couponType(CouponType.LIMITED)
                    .build();

            coupon.increaseIssuedQuantity();

            assertThatThrownBy(coupon::increaseIssuedQuantity)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_ISSUE_LIMIT_EXCEEDED.getMessage());
        }
    }
}