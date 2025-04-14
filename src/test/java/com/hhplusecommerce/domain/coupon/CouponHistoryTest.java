package com.hhplusecommerce.domain.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CouponHistoryTest {

    private static final Long USER_ID = 1L;
    private static final String COUPON_NAME = "10% 할인 쿠폰";
    private static final CouponDiscountType FIXED_RATE = CouponDiscountType.FIXED_RATE;
    private static final BigDecimal DISCOUNT_PERCENT_10 = BigDecimal.valueOf(10);
    private static final int MAX_ISSUE_QUANTITY = 100;
    private static final LocalDate VALID_START_DATE = LocalDate.now().minusDays(1);
    private static final LocalDate VALID_END_DATE = LocalDate.now().plusDays(7);

    private Coupon createValidCoupon() {
        Coupon coupon = Coupon.builder()
                .couponName(COUPON_NAME)
                .discountType(FIXED_RATE)
                .discountValue(DISCOUNT_PERCENT_10)
                .maxQuantity(MAX_ISSUE_QUANTITY)
                .validStartDate(VALID_START_DATE)
                .validEndDate(VALID_END_DATE)
                .issuedQuantity(0)
                .couponType(CouponType.LIMITED)
                .build();

        ReflectionTestUtils.setField(coupon, "id", 1L); // ID 수동 세팅
        return coupon;
    }

    @Test
    void 쿠폰_발급시_상태는_AVAILABLE이고_사용기록은_null이다() {
        Coupon coupon = createValidCoupon();

        CouponHistory history = CouponHistory.issue(USER_ID, coupon);

        assertThat(history.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.AVAILABLE);
        assertThat(history.getUseDate()).isNull();
    }

    @Test
    void 쿠폰_사용시_상태는_USED로_변경되고_useDate가_세팅된다() {
        Coupon coupon = createValidCoupon();
        CouponHistory history = CouponHistory.issue(USER_ID, coupon);

        history.use();

        assertThat(history.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.USED);
        assertThat(history.getUseDate()).isNotNull();
    }

    @Test
    void 사용가능한_쿠폰은_사용할_수_있다() {
        Coupon coupon = createValidCoupon();
        CouponHistory history = CouponHistory.issue(USER_ID, coupon);

        assertThat(history.isAvailable()).isTrue();
    }

    @Test
    void 쿠폰의_사용여부는_isAvailable로_확인할_수_있다() {
        Coupon coupon = createValidCoupon();

        CouponHistory available = CouponHistory.issue(USER_ID, coupon);
        assertThat(available.isAvailable()).isTrue();

        available.use();
        assertThat(available.isAvailable()).isFalse();
    }
}
