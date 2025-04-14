package com.hhplusecommerce.interfaces.payment;

import com.hhplusecommerce.domain.payment.PaymentCommand;
import com.hhplusecommerce.domain.payment.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "결제 요청")
public class PaymentRequest {

    @NotNull(message = "orderId는 필수입니다.")
    @Schema(description = "주문 ID", example = "1001")
    private Long orderId;

    @NotNull(message = "결제 수단은 필수입니다.")
    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    private PaymentMethod paymentGateway;

    public PaymentCommand toCommand() {
        return new PaymentCommand(orderId, paymentGateway);
    }
}
