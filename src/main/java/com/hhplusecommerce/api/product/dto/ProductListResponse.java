package com.hhplusecommerce.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductListResponse(
        @Schema(description = "상품 ID", example = "1") Long productId,
        @Schema(description = "상품 이름", example = "MacBook Pro") String name,
        @Schema(description = "상품 가격", example = "2390000") Long price
) {
    public static ProductListResponse of(Long productId, String name, Long price) {
        return new ProductListResponse(productId, name, price);
    }
}
