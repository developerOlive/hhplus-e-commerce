package com.hhplusecommerce.api.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentResponse(
        @Schema(description = "결제 ID", example = "5001") Long paymentId,
        @Schema(description = "결제 금액", example = "20000") Long payAmount,
        @Schema(description = "결제 상태", example = "SUCCESS") String status
) {
    public static PaymentResponse of(Long id, Long amount, String status) {
        return new PaymentResponse(id, amount, status);
    }
}
