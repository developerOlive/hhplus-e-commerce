package com.hhplusecommerce.interfaces.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderResponse(
        @Schema(description = "주문 ID", example = "1001") Long orderId,
        @Schema(description = "총 주문 금액", example = "25000") Long totalAmount,
        @Schema(description = "할인 적용 후 최종 결제 금액", example = "20000") Long finalAmount,
        @Schema(description = "주문 상태", example = "PENDING") String status
) {
    public static OrderResponse of(Long orderId, Long total, Long finalAmount, String status) {
        return new OrderResponse(orderId, total, finalAmount, status);
    }
}
