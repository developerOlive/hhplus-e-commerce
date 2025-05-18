package com.hhplusecommerce.integration.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.model.CouponHistory;
import com.hhplusecommerce.domain.coupon.repository.CouponHistoryRepository;
import com.hhplusecommerce.domain.coupon.repository.CouponRepository;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.type.CouponDiscountType;
import com.hhplusecommerce.domain.coupon.type.CouponStatus;
import com.hhplusecommerce.domain.coupon.type.CouponType;
import com.hhplusecommerce.domain.coupon.type.CouponUsageStatus;
import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;

class CouponServiceIntegrationTest extends IntegrationTestSupport {

    private static final Long USER_ID = 1L;
    private static final BigDecimal TOTAL_AMOUNT = BigDecimal.valueOf(20_000);
    private static final int DEFAULT_MAX_QUANTITY = 10;
    private static final int ISSUED_QUANTITY_EXCEED = 1;

    private static final BigDecimal FIXED_DISCOUNT = BigDecimal.valueOf(3_000);
    private static final BigDecimal HIGH_DISCOUNT = BigDecimal.valueOf(5_000);
    private static final BigDecimal RATE_DISCOUNT_PERCENT = BigDecimal.valueOf(10);
    private static final BigDecimal EXPECTED_RATE_DISCOUNT = BigDecimal.valueOf(2_000);

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponHistoryRepository couponHistoryRepository;

    @Nested
    class 쿠폰_발급 {

        @Test
        void 쿠폰을_정상적으로_발급한다() {
            Coupon coupon = createCoupon(FIXED_DISCOUNT, DEFAULT_MAX_QUANTITY);

            Long issuedId = couponService.issueCoupon(new CouponCommand(USER_ID, coupon.getId()));

            CouponHistory history = couponHistoryRepository.findById(issuedId).orElseThrow();

            assertAll(
                    () -> assertThat(history.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(history.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.AVAILABLE),
                    () -> assertThat(history.getCoupon()).isNotNull()
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 쿠폰_생성시_발급수량이_0_또는_음수이면_예외가_발생한다(int quantity) {
            assertThatThrownBy(() ->
                    Coupon.builder()
                            .couponName("테스트쿠폰")
                            .discountType(CouponDiscountType.FIXED_AMOUNT)
                            .discountValue(FIXED_DISCOUNT)
                            .maxQuantity(quantity)
                            .issuedQuantity(0)
                            .validStartDate(LocalDate.now())
                            .validEndDate(LocalDate.now().plusDays(7))
                            .couponType(CouponType.LIMITED)
                            .build()
            ).isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_COUPON_QUANTITY.getMessage());
        }

        @Test
        void 발급수량을_초과하면_쿠폰발급시_예외가_발생한다() {
            Coupon coupon = Coupon.builder()
                    .couponName("테스트쿠폰")
                    .discountType(CouponDiscountType.FIXED_AMOUNT)
                    .discountValue(FIXED_DISCOUNT)
                    .maxQuantity(ISSUED_QUANTITY_EXCEED)
                    .issuedQuantity(ISSUED_QUANTITY_EXCEED)
                    .validStartDate(LocalDate.now())
                    .validEndDate(LocalDate.now().plusDays(7))
                    .couponType(CouponType.LIMITED)
                    .build();

            couponRepository.save(coupon);

            assertThatThrownBy(() -> couponService.issueCoupon(new CouponCommand(USER_ID, coupon.getId())))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 쿠폰_사용처리 {

        @Test
        void 사용가능한_쿠폰이_정상_사용처리되면_상태와_사용날짜가_변경된다() {
            Coupon coupon = createCoupon(HIGH_DISCOUNT, DEFAULT_MAX_QUANTITY);
            Long issueId = couponService.issueCoupon(new CouponCommand(USER_ID, coupon.getId()));

            couponService.useCoupon(USER_ID, issueId);

            CouponHistory used = couponHistoryRepository.findById(issueId).orElseThrow();

            assertAll(
                    () -> assertThat(used.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.USED),
                    () -> assertThat(used.getUseDate()).isNotNull()
            );
        }
    }

    @Nested
    class 쿠폰_할인_계산 {

        @Test
        void 정액_쿠폰은_정해진_할인값을_반환한다() {
            Coupon coupon = createCoupon(HIGH_DISCOUNT, DEFAULT_MAX_QUANTITY, CouponDiscountType.FIXED_AMOUNT);
            Long issueId = couponService.issueCoupon(new CouponCommand(USER_ID, coupon.getId()));

            BigDecimal discount = couponService.calculateDiscount(USER_ID, issueId, TOTAL_AMOUNT);

            assertThat(discount).isEqualByComparingTo(HIGH_DISCOUNT);
        }

        @Test
        void 정률_쿠폰은_총액에_비례한_할인값을_반환한다() {
            Coupon coupon = createCoupon(RATE_DISCOUNT_PERCENT, DEFAULT_MAX_QUANTITY, CouponDiscountType.FIXED_RATE);
            Long issueId = couponService.issueCoupon(new CouponCommand(USER_ID, coupon.getId()));

            BigDecimal discount = couponService.calculateDiscount(USER_ID, issueId, TOTAL_AMOUNT);

            assertThat(discount).isEqualByComparingTo(EXPECTED_RATE_DISCOUNT);
        }
    }

    private Coupon createCoupon(BigDecimal discountValue, int maxQuantity) {
        return createCoupon(discountValue, maxQuantity, CouponDiscountType.FIXED_AMOUNT);
    }

    private Coupon createCoupon(BigDecimal discountValue, int maxQuantity, CouponDiscountType type) {
        Coupon coupon = Instancio.of(Coupon.class)
                .set(field(Coupon::getCouponName), "테스트쿠폰")
                .set(field(Coupon::getDiscountType), type)
                .set(field(Coupon::getDiscountValue), discountValue)
                .set(field(Coupon::getMaxQuantity), maxQuantity)
                .set(field(Coupon::getIssuedQuantity), 0)
                .set(field(Coupon::getValidStartDate), LocalDate.now().minusDays(1))
                .set(field(Coupon::getValidEndDate), LocalDate.now().plusDays(5))
                .set(field(Coupon::getCouponType), CouponType.LIMITED)
                .set(field(Coupon::getCouponStatus), CouponStatus.ACTIVE)
                .create();

        return couponRepository.save(coupon);
    }
}
