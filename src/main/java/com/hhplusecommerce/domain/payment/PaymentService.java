package com.hhplusecommerce.domain.payment;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 성공 처리
     */
    @Transactional
    public Payment pay(Long orderId, BigDecimal payAmount) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paidAmount(payAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        payment.complete();
        return paymentRepository.save(payment);
    }

    /**
     * 결제 실패 처리
     */
    @Transactional
    public Payment fail(Long orderId, BigDecimal payAmount) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paidAmount(payAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        payment.fail();
        return paymentRepository.save(payment);
    }

    /**
     * 결제 단건 조회
     */
    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorType.PAYMENT_NOT_FOUND));
    }
}
