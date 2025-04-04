package com.hhplusecommerce.api.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상품 정보")
public record OrderItemRequest(
        @Schema(description = "상품 ID", example = "10") Long productId,
        @Schema(description = "주문 수량", example = "2") int quantity
) {}
