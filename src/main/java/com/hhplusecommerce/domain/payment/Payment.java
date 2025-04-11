package com.hhplusecommerce.domain.payment;

import jakarta.persistence.*;
import lombok.*;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보 (결제 금액, 상태 등)를 관리
 *
 * - 주문에 대한 결제 금액 및 상태 저장
 * - 결제 성공/실패 상태 처리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private BigDecimal paidAmount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;

    @Builder
    public Payment(Long orderId, BigDecimal paidAmount, PaymentStatus paymentStatus) {
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_PAYMENT_AMOUNT);
        }
        this.orderId = orderId;
        this.paidAmount = paidAmount;
        this.paymentStatus = paymentStatus;
        this.createdAt = LocalDateTime.now();
    }

    public void complete() {
        this.paymentStatus = this.paymentStatus.complete();
    }

    public void fail() {
        this.paymentStatus = this.paymentStatus.fail();
    }
}
