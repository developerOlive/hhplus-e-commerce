package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.model.CouponHistory;
import com.hhplusecommerce.domain.coupon.repository.CouponHistoryRepository;
import com.hhplusecommerce.domain.coupon.repository.CouponRepository;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.type.CouponDiscountType;
import com.hhplusecommerce.domain.coupon.type.CouponType;
import com.hhplusecommerce.domain.coupon.type.CouponUsageStatus;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COUPON_ID = 10L;
    private static final Long COUPON_ISSUE_ID = 100L;
    private static final BigDecimal TOTAL_AMOUNT = BigDecimal.valueOf(10000);
    private static final String NAME = "테스트 쿠폰";
    private static final int MAX_QUANTITY = 10;
    private static final LocalDate START = LocalDate.now().minusDays(1);
    private static final LocalDate END = LocalDate.now().plusDays(1);
    private static final BigDecimal DISCOUNT_VALUE = BigDecimal.valueOf(1000);

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponHistoryRepository couponHistoryRepository;

    @Mock
    private Order order;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .couponName(NAME)
                .discountType(CouponDiscountType.FIXED_AMOUNT)
                .discountValue(DISCOUNT_VALUE)
                .maxQuantity(MAX_QUANTITY)
                .validStartDate(START)
                .validEndDate(END)
                .issuedQuantity(0)
                .couponType(CouponType.LIMITED)
                .build();

        ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
    }

    @Nested
    class 쿠폰_발급_테스트 {

        @Test
        void 정상적으로_쿠폰이_발급된다() {
            CouponCommand command = new CouponCommand(USER_ID, COUPON_ID);
            when(couponRepository.increaseIssuedQuantityIfNotExceeded(COUPON_ID)).thenReturn(1);
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(coupon));

            couponService.issueCoupon(command);

            verify(couponHistoryRepository).save(any(CouponHistory.class));
        }

        @Test
        void 존재하지_않는_쿠폰이면_발급할_수_없다() {
            CouponCommand command = new CouponCommand(USER_ID, COUPON_ID);
            when(couponRepository.increaseIssuedQuantityIfNotExceeded(COUPON_ID)).thenReturn(1);
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        void 발급_수량을_초과하면_예외가_발생한다() {
            CouponCommand command = new CouponCommand(USER_ID, COUPON_ID);
            when(couponRepository.increaseIssuedQuantityIfNotExceeded(COUPON_ID)).thenReturn(0);

            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_NO_STOCK.getMessage());
        }
    }

    @Nested
    class 할인_금액_계산_테스트 {

        @Test
        void 쿠폰이_없으면_할인금액은_0원이다() {
            BigDecimal discount = couponService.calculateDiscount(USER_ID, null, TOTAL_AMOUNT);
            assertThat(discount).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void 사용가능한_쿠폰으로_정확한_할인금액을_계산한다() {
            ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
            CouponHistory history = CouponHistory.issue(USER_ID, coupon);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));

            BigDecimal discount = couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT);

            assertThat(discount).isEqualTo(DISCOUNT_VALUE);
        }
    }

    @Nested
    class 쿠폰_사용_처리_테스트 {

        @Test
        void 사용가능한_쿠폰은_정상적으로_사용된다() {
            ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
            CouponHistory history = CouponHistory.issue(USER_ID, coupon);

            when(order.hasCoupon()).thenReturn(true);
            when(order.getUserId()).thenReturn(USER_ID);
            when(order.getCouponIssueId()).thenReturn(COUPON_ISSUE_ID);
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));

            couponService.useCoupon(order);
            assertThat(history.isAvailable()).isFalse();
            assertThat(history.getCouponUsageStatus()).isEqualTo(CouponUsageStatus.USED);
            assertThat(history.getUseDate()).isNotNull();
        }
    }

    @Nested
    class 쿠폰_사용_및_할인_계산_예외_테스트 {

        @Test
        void 존재하지_않는_쿠폰은_예외가_발생한다() {
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_사용된_쿠폰은_예외가_발생한다() {
            CouponHistory history = CouponHistory.issue(USER_ID, coupon);
            history.use(); // 쿠폰 사용 처리
            when(couponHistoryRepository.findById(COUPON_ISSUE_ID)).thenReturn(Optional.of(history));

            assertThatThrownBy(() -> couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_ALREADY_USED.getMessage());
        }
    }
}
