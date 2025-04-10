package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long COUPON_ID = 10L;
    private static final Long COUPON_ISSUE_ID = 100L;
    private static final BigDecimal TOTAL_AMOUNT = BigDecimal.valueOf(10000);

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponHistoryRepository couponHistoryRepository;

    private Coupon createValidCoupon() {
        return Coupon.builder()
                .couponName("테스트 쿠폰")
                .discountType(CouponDiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(1000))
                .maxQuantity(10)
                .validStartDate(LocalDate.now().minusDays(1))
                .validEndDate(LocalDate.now().plusDays(1))
                .build();
    }

    @Nested
    class 쿠폰_발급_테스트 {

        @Test
        void 정상적으로_쿠폰이_발급된다() {
            // given
            Coupon coupon = createValidCoupon();
            CouponCommand command = new CouponCommand(USER_ID, COUPON_ID);
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(coupon));

            // when
            couponService.issueCoupon(command);

            // then
            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
            verify(couponHistoryRepository).save(any(CouponHistory.class));
        }

        @Test
        void 존재하지_않는_쿠폰이면_발급할_수_없다() {
            // given
            CouponCommand command = new CouponCommand(USER_ID, COUPON_ID);
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 할인_금액_계산_테스트 {

        private static final BigDecimal DISCOUNT_VALUE = BigDecimal.valueOf(1000);

        @Test
        void 쿠폰이_없으면_할인금액은_0원이다() {
            // when
            BigDecimal discount = couponService.calculateDiscount(USER_ID, null, TOTAL_AMOUNT);

            // then
            assertThat(discount).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void 사용가능한_쿠폰으로_정확한_할인금액을_계산한다() {
            // given
            Coupon coupon = createValidCoupon();
            ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
            CouponHistory history = CouponHistory.issue(USER_ID, coupon);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(coupon));

            // when
            BigDecimal discount = couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT);

            // then
            assertThat(discount).isEqualTo(DISCOUNT_VALUE);
        }
    }

    @Nested
    class 쿠폰_사용_처리_테스트 {

        @Test
        void 사용가능한_쿠폰은_정상적으로_사용된다() {
            // given
            Coupon coupon = createValidCoupon();
            ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
            CouponHistory history = CouponHistory.issue(USER_ID, coupon);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(coupon));

            // when
            couponService.markUsed(USER_ID, COUPON_ISSUE_ID);

            // then
            assertThat(history.isAvailable()).isFalse();
        }
    }

    @Nested
    class 쿠폰_사용_및_할인_계산_예외_테스트 {

        @Test
        void 다른_사람의_쿠폰은_할인에_사용할_수_없다() {
            // given
            Coupon coupon = createValidCoupon();
            CouponHistory history = CouponHistory.issue(OTHER_USER_ID, coupon);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));

            // when & then
            assertThatThrownBy(() -> couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.UNAUTHORIZED_COUPON_ACCESS.getMessage());
        }

        @Test
        void 존재하지_않는_쿠폰은_할인에_사용할_수_없다() {
            // given
            Coupon coupon = createValidCoupon();
            ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
            CouponHistory history = CouponHistory.issue(USER_ID, coupon);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_사용자의_쿠폰은_사용처리할_수_없다() {
            // given
            Coupon coupon = createValidCoupon();
            CouponHistory history = CouponHistory.issue(OTHER_USER_ID, coupon);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));

            // when & then
            assertThatThrownBy(() -> couponService.markUsed(USER_ID, COUPON_ISSUE_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.UNAUTHORIZED_COUPON_ACCESS.getMessage());
        }
    }
}
