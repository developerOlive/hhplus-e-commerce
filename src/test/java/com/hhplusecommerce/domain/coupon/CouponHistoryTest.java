package com.hhplusecommerce.domain.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CouponHistoryTest {

    private Coupon coupon;
    private CouponHistory couponHistory;

    @BeforeEach
    void setUp() {
        coupon = new Coupon("10% 할인 쿠폰", CouponDiscountType.FIXED_RATE, new BigDecimal("10"), 100, LocalDate.now(), LocalDate.now().plusDays(30));
        couponHistory = CouponHistory.issue(1L, coupon); // 발급된 쿠폰 히스토리 생성
    }

    @Nested
    class 쿠폰_발급_성공 {

        @Test
        void 정상적으로_쿠폰_발급() {
            assertThat(couponHistory.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.AVAILABLE);
            assertThat(couponHistory.getPublishDate()).isEqualTo(LocalDate.now());
        }

        @Test
        void 쿠폰_발급_수량이_0일때_변경된_잔액을_검증() {
            // 발급 수량 0으로 설정 후, 발급 시도
            coupon.increaseIssuedQuantity(); // 발급 시도

            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        }
    }

    @Nested
    class 쿠폰_사용_성공 {

        @Test
        void 정상적으로_쿠폰_사용() {
            couponHistory.markUsed();

            assertThat(couponHistory.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.USED);
            assertThat(couponHistory.getUseDate()).isEqualTo(LocalDate.now());
        }
    }
}
