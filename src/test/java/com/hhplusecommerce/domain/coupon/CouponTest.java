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
                    .build();

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
            Coupon coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(FIXED_DISCOUNT_500_WON)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .build();

            BigDecimal discount = coupon.discountFor(ORDER_TOTAL_400_WON);

            assertThat(discount).isEqualTo(ORDER_TOTAL_400_WON); // 할인액은 주문 금액을 초과하지 않음
        }

        @Test
        void 총금액이_0이면_할인금액은_0() {
            Coupon coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(FIXED_DISCOUNT_500_WON)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .build();

            BigDecimal discount = coupon.discountFor(ZERO_AMOUNT);

            assertThat(discount).isEqualTo(ZERO_AMOUNT);
        }

        @Test
        void 총금액이_null이면_할인금액은_0() {
            Coupon coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(FIXED_DISCOUNT_500_WON)
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .build();

            BigDecimal discount = coupon.discountFor(null);

            assertThat(discount).isEqualTo(ZERO_AMOUNT);
        }
    }

    @Nested
    class 쿠폰_발급 {

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
                    .build();
        }

        @Test
        void 발급가능한_상태에서_발급을_처리하면_발급수량이_증가한다() {
            coupon.confirmCouponIssue();

            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        }

        @Test
        void 수량제한_쿠폰은_최대_발급수량에_도달하면_발급할_수_없다() {
            coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(500))
                    .maxQuantity(MAX_QUANTITY)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(MAX_QUANTITY) // 이미 최대 발급 수량으로 설정
                    .couponType(CouponType.LIMITED) // 수량 제한이 있는 쿠폰
                    .build();

            assertThatThrownBy(() -> coupon.confirmCouponIssue())
                    .isInstanceOf(CustomException.class)

                    .hasMessage(ErrorType.COUPON_ISSUE_LIMIT_EXCEEDED.getMessage());
        }
    }
    @Nested
    class 발급_가능_여부_판단 {

        @Test
        void 수량제한이_없는_쿠폰은_발급수량에_관계없이_발급가능하다() {
            coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(500))
                    .maxQuantity(MAX_QUANTITY)  // 최대 발급 수량 설정
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0) // 발급 수량 0으로 설정
                    .couponType(CouponType.UNLIMITED) // 수량 제한이 없는 무제한 쿠폰 설정
                    .build();

            assertThat(coupon.isIssuable()).isTrue(); // 발급 가능 여부 확인
        }

        @Test
        void 수량제한이_있는_쿠폰에서_발급수량이_최대_수량에_도달한_경우_발급불가하다() {
            coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(500))
                    .maxQuantity(MAX_QUANTITY)  // 최대 발급 수량 설정
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(MAX_QUANTITY) // 이미 최대 발급 수량으로 설정
                    .couponType(CouponType.LIMITED) // 수량 제한이 있는 쿠폰 설정
                    .build();

            assertThat(coupon.isIssuable()).isFalse();
        }

        @Test
        void 무제한_쿠폰은_발급수량에_관계없이_발급가능하다() {
            coupon = Coupon.builder()
                    .couponName(NAME)
                    .discountType(FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(500))
                    .maxQuantity(Integer.MAX_VALUE)
                    .validStartDate(START)
                    .validEndDate(END)
                    .issuedQuantity(0)
                    .couponType(CouponType.UNLIMITED)
                    .build();

            assertThat(coupon.isIssuable()).isTrue();
        }
    }
}
