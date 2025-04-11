package com.hhplusecommerce.interfaces.payment;

import com.hhplusecommerce.application.payment.PaymentResult;
import com.hhplusecommerce.domain.payment.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record PaymentResponse(
        @Schema(description = "결제 ID", example = "5001") Long paymentId,
        @Schema(description = "결제 금액", example = "20000") BigDecimal paidAmount,
        @Schema(description = "결제 상태", example = "SUCCESS") PaymentStatus paymentStatus
) {
    public static PaymentResponse from(PaymentResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.paidAmount(),
                result.paymentStatus()
        );
    }
}
