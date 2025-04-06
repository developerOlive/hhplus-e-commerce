package com.hhplusecommerce.interfaces.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "결제 요청")
public class PaymentRequest {

    @Schema(description = "주문 ID", example = "1001")
    private Long orderId;

    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    private String paymentGateway;
}
