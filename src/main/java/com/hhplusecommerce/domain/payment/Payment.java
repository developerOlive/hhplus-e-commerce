package com.hhplusecommerce.domain.payment;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보 (결제 금액, 상태 등)를 관리
 * <p>
 * - 주문에 대한 결제 금액 및 상태 저장
 * - 결제 성공/실패 상태 처리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    @Column(precision = 10, scale = 0)
    private BigDecimal paidAmount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;

    @Builder
    public Payment(Long orderId, BigDecimal paidAmount, PaymentStatus paymentStatus) {
        if (orderId == null) {
            throw new CustomException(ErrorType.INVALID_PAYMENT_ORDER_ID);
        }
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_PAYMENT_AMOUNT);
        }
        if (paymentStatus == null) {
            throw new CustomException(ErrorType.INVALID_PAYMENT_STATUS);
        }

        this.orderId = orderId;
        this.paidAmount = paidAmount;
        this.paymentStatus = paymentStatus;
        this.createdAt = LocalDateTime.now();
    }

    public static Payment createPending(Long orderId, BigDecimal amount) {
        return Payment.builder()
                .orderId(orderId)
                .paidAmount(amount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public void complete() {
        this.paymentStatus = this.paymentStatus.complete();
    }

    public void fail() {
        this.paymentStatus = this.paymentStatus.fail();
    }
}
