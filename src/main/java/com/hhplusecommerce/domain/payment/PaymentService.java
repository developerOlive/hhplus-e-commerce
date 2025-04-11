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
    public Payment completePayment(Long orderId, BigDecimal amount) {
        Payment payment = Payment.createPending(orderId, amount);
        payment.complete();
        return paymentRepository.save(payment);
    }

    /**
     * 결제 실패 처리
     */
    @Transactional
    public Payment failPayment(Long orderId, BigDecimal amount) {
        Payment payment = Payment.createPending(orderId, amount);
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
