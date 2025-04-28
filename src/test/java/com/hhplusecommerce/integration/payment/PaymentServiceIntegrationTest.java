package com.hhplusecommerce.integration.payment;

import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.domain.payment.Payment;
import com.hhplusecommerce.domain.payment.PaymentService;
import com.hhplusecommerce.domain.payment.PaymentStatus;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class PaymentServiceIntegrationTest extends IntegrationTestSupport {

    private static final Long ORDER_ID = 1L;
    private static final BigDecimal VALID_AMOUNT = BigDecimal.valueOf(15_000);

    @Autowired
    private PaymentService paymentService;

    @Nested
    class 결제_성공_처리 {

        @Test
        void 결제가_정상적으로_성공하면_상태는_SUCCESS가_된다() {
            Payment payment = paymentService.completePayment(ORDER_ID, VALID_AMOUNT);

            assertAll(
                    () -> assertThat(payment.getOrderId()).isEqualTo(ORDER_ID),
                    () -> assertThat(payment.getPaidAmount()).isEqualByComparingTo(VALID_AMOUNT),
                    () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS),
                    () -> assertThat(payment.getCreatedAt()).isNotNull()
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 결제금액이_0원_또는_음수면_예외가_발생한다(int invalidAmount) {
            BigDecimal amount = BigDecimal.valueOf(invalidAmount);
            assertThatThrownBy(() -> paymentService.completePayment(ORDER_ID, amount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_PAYMENT_AMOUNT.getMessage());
        }
    }

    @Nested
    class 결제_실패_처리 {

        @Test
        void 결제가_실패로_처리되면_상태는_FAILED가_된다() {
            Payment payment = paymentService.failPayment(ORDER_ID, VALID_AMOUNT);

            assertAll(
                    () -> assertThat(payment.getOrderId()).isEqualTo(ORDER_ID),
                    () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED)
            );
        }
    }

    @Nested
    class 결제_단건_조회 {

        private static final Long NOT_FOUND_ID = 9999L;

        @Test
        void 존재하는_결제ID로_조회하면_해당_엔티티를_반환한다() {
            Payment saved = paymentService.completePayment(ORDER_ID, VALID_AMOUNT);

            Payment found = paymentService.getPayment(saved.getId());

            assertThat(found.getId()).isEqualTo(saved.getId());
        }

        @Test
        void 존재하지_않는_결제ID는_예외가_발생한다() {
            assertThatThrownBy(() -> paymentService.getPayment(NOT_FOUND_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.PAYMENT_NOT_FOUND.getMessage());
        }
    }
}
