package com.hhplusecommerce.interfaces.order;

import com.hhplusecommerce.application.order.OrderResult;
import com.hhplusecommerce.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderResponse(
        @Schema(description = "주문 ID", example = "1001") Long orderId,
        @Schema(description = "총 주문 금액", example = "25000") BigDecimal totalAmount,
        @Schema(description = "할인 적용 후 최종 결제 금액", example = "20000") BigDecimal finalAmount,
        @Schema(description = "주문 상태", example = "PAYMENT_WAIT") OrderStatus orderStatus
) {
    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.totalAmount(),
                result.finalAmount(),
                result.orderStatus()
        );
    }
}
