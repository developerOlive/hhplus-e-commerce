package com.hhplusecommerce.domain.payment;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    private final Long orderId = 1L;
    private final Long paymentId = 100L;
    private final BigDecimal payAmount = new BigDecimal("10000");

    @Nested
    @DisplayName("결제 성공 처리 테스트")
    class PaySuccessTest {

        @Test
        void 결제_대기중인_상태에서만_결제가_성공_처리될_수_있다() {
            // given
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Payment savedPayment = paymentService.completePayment(orderId, payAmount);

            // then
            assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(savedPayment.getPaidAmount()).isEqualByComparingTo(payAmount);
            verify(paymentRepository).save(any(Payment.class));
        }


        @Test
        void 결제_대기중이_아닌_상태에서_결제를_완료하면_예외가_발생한다() {
            // given
            Payment alreadySuccess = Payment.builder()
                    .orderId(orderId)
                    .paidAmount(payAmount)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .build();

            // then
            assertThatThrownBy(alreadySuccess::complete)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_PAYMENT_STATUS_TO_COMPLETE.getMessage());
        }
    }

    @Nested
    @DisplayName("결제 실패 처리 테스트")
    class PayFailTest {

        @Test
        void 결제_대기중_상태에서_결제가_실패하면_FAILED_상태로_변경된다() {
            // given
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Payment savedPayment = paymentService.failPayment(orderId, payAmount);

            // then
            assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(savedPayment.getPaidAmount()).isEqualByComparingTo(payAmount);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        void 결제_대기중이_아닌_상태에서_결제를_실패처리하면_예외가_발생한다() {
            // given
            Payment alreadyFailed = Payment.builder()
                    .orderId(orderId)
                    .paidAmount(payAmount)
                    .paymentStatus(PaymentStatus.FAILED)
                    .build();

            // then
            assertThatThrownBy(alreadyFailed::fail)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_PAYMENT_STATUS_TO_FAIL.getMessage());
        }

    }

    @Nested
    class 결제_조회_테스트 {

        @Test
        void 존재하는_결제ID로_조회하면_결제정보가_반환된다() {
            // given
            Payment found = Payment.builder()
                    .orderId(orderId)
                    .paidAmount(payAmount)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .build();

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(found));

            // when
            Payment result = paymentService.getPayment(paymentId);

            // then
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            verify(paymentRepository).findById(paymentId);
        }

        @Test
        void 존재하지_않는_결제ID로_조회하면_예외가_발생한다() {
            // given
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.PAYMENT_NOT_FOUND.getMessage());
        }
    }
}
