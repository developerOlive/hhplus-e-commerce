package com.hhplusecommerce.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDetailResponse(
        @Schema(description = "상품 ID", example = "1") Long productId,
        @Schema(description = "상품 이름", example = "MacBook Pro") String name,
        @Schema(description = "카테고리", example = "노트북") String category,
        @Schema(description = "상품 가격", example = "2390000") Long price,
        @Schema(description = "재고 수량", example = "10") int stock
) {
    public static ProductDetailResponse of(Long productId, String name, String category, Long price, int stock) {
        return new ProductDetailResponse(productId, name, category, price, stock);
    }
}
