package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.hhplusecommerce.domain.coupon.CouponDiscountType.FIXED_AMOUNT;
import static org.assertj.core.api.Assertions.*;

class CouponTest {

    private static final String NAME = "테스트 쿠폰";
    private static final int MAX_QUANTITY = 3;
    private static final LocalDate START = LocalDate.now().minusDays(1);
    private static final LocalDate END = LocalDate.now().plusDays(1);

    @Nested
    class 할인금액_유효성검사 {

        @Test
        void 할인금액이_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Coupon(NAME, FIXED_AMOUNT, null, MAX_QUANTITY, START, END))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_COUPON_VALUE.getMessage());
        }

        @Test
        void 할인금액이_0이하면_예외가_발생한다() {
            assertThatThrownBy(() -> new Coupon(NAME, CouponDiscountType.FIXED_RATE, BigDecimal.ZERO, MAX_QUANTITY, START, END))
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
            Coupon validCoupon = new Coupon(NAME, CouponDiscountType.FIXED_RATE, DISCOUNT_PERCENT_10, MAX_QUANTITY, START, END);

            assertThat(validCoupon.isAvailable()).isTrue();
        }

        @Test
        void 유효기간이_지난_쿠폰은_사용_불가능하다() {
            Coupon expiredCoupon = new Coupon(NAME, CouponDiscountType.FIXED_RATE, DISCOUNT_PERCENT_10, MAX_QUANTITY, EXPIRED_START, EXPIRED_END);

            assertThat(expiredCoupon.isAvailable()).isFalse();
        }
    }

    @Nested
    class 발급수량_증가 {

        private static final BigDecimal DISCOUNT_VALUE_500 = BigDecimal.valueOf(500);
        private static final int MAX_QUANTITY_LIMIT = 1;

        @Test
        void 발급수량이_정상적으로_1_증가한다() {
            Coupon coupon = new Coupon(NAME, FIXED_AMOUNT, DISCOUNT_VALUE_500, MAX_QUANTITY, START, END);

            coupon.increaseIssuedQuantity();

            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        }

        @Test
        void 발급수량이_최대치를_초과하면_예외가_발생한다() {
            Coupon coupon = new Coupon(NAME, FIXED_AMOUNT, DISCOUNT_VALUE_500, MAX_QUANTITY_LIMIT, START, END);

            coupon.increaseIssuedQuantity();

            assertThatThrownBy(coupon::increaseIssuedQuantity)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_ISSUE_LIMIT_EXCEEDED.getMessage());
        }
    }

    @Nested
    class 할인계산 {

        private static final BigDecimal FIXED_DISCOUNT_500_WON = BigDecimal.valueOf(500);
        private static final BigDecimal ORDER_TOTAL_400_WON = BigDecimal.valueOf(400);
        private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;

        @Test
        void 정액_할인은_총금액을_초과하지_않는다() {
            Coupon coupon = new Coupon(NAME, FIXED_AMOUNT, FIXED_DISCOUNT_500_WON, MAX_QUANTITY, START, END);

            BigDecimal discount = coupon.discountFor(ORDER_TOTAL_400_WON);

            assertThat(discount).isEqualTo(ORDER_TOTAL_400_WON); // 할인액은 주문 금액을 초과하지 않음
        }

        @Test
        void 총금액이_0이면_할인금액은_0() {
            Coupon coupon = new Coupon(NAME, FIXED_AMOUNT, FIXED_DISCOUNT_500_WON, MAX_QUANTITY, START, END);

            BigDecimal discount = coupon.discountFor(ZERO_AMOUNT);

            assertThat(discount).isEqualTo(ZERO_AMOUNT);
        }

        @Test
        void 총금액이_null이면_할인금액은_0() {
            Coupon coupon = new Coupon(NAME, FIXED_AMOUNT, FIXED_DISCOUNT_500_WON, MAX_QUANTITY, START, END);

            BigDecimal discount = coupon.discountFor(null);

            assertThat(discount).isEqualTo(ZERO_AMOUNT);
        }
    }
}
