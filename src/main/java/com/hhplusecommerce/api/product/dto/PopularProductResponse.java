package com.hhplusecommerce.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PopularProductResponse(
        @Schema(description = "상품 ID", example = "1") Long productId,
        @Schema(description = "상품명", example = "MacBook Pro") String name,
        @Schema(description = "가격", example = "2390000") Long price,
        @Schema(description = "총 판매 수량", example = "200") int totalSold
) {}
