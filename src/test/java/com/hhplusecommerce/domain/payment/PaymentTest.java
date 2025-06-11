package com.hhplusecommerce.domain.payment;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final Long ORDER_ID = 1L;
    private static final BigDecimal VALID_PAYMENT_AMOUNT = new BigDecimal("25000");

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment(ORDER_ID, VALID_PAYMENT_AMOUNT, PaymentStatus.PENDING);
    }

    @Nested
    class 결제상태_변경_성공 {

        @Test
        void 결제_대기_상태에서_결제_성공으로_변경된다() {
            // when
            payment.complete();
            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        void 결제_대기_상태에서_결제_실패로_변경된다() {
            // when
            payment.fail();
            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    @Nested
    class 결제상태_변경_실패 {

        @Test
        void 결제_실패_상태에서_결제_성공_처리하려고_하면_예외가_발생한다() {
            // given
            payment.fail();

            // when & then
            assertThatThrownBy(payment::complete)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_PAYMENT_STATUS_TO_COMPLETE.getMessage());
        }

        @Test
        void 결제_성공_상태에서_결제_실패_처리하려고_하면_예외가_발생한다() {
            // given
            payment.complete();

            // when & then
            assertThatThrownBy(payment::fail)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_PAYMENT_STATUS_TO_FAIL.getMessage());
        }

    }

    @Nested
    class 결제금액_검증 {

        @ParameterizedTest
        @ValueSource(doubles = {0, -1}) // 0과 음수 값 테스트
        void 결제금액이_0_이하일_경우_예외가_발생한다(double amount) {
            assertThatThrownBy(() -> new Payment(ORDER_ID, BigDecimal.valueOf(amount), PaymentStatus.PENDING))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("결제 금액은 0 이상이어야 합니다.");
        }

        @Test
        void 결제금액이_양수일_경우_정상적으로_생성된다() {
            payment = new Payment(ORDER_ID, VALID_PAYMENT_AMOUNT, PaymentStatus.PENDING);
            assertThat(payment.getPaidAmount()).isEqualTo(VALID_PAYMENT_AMOUNT);
        }
    }
}
