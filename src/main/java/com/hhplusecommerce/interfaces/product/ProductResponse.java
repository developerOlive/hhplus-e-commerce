package com.hhplusecommerce.interfaces.product;

import io.swagger.v3.oas.annotations.media.Schema;
import com.hhplusecommerce.applicatoin.product.result.ProductResult;

public class ProductResponse {

    public static class ProductListResponse {
        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Schema(description = "상품 이름", example = "MacBook Pro")
        private String name;

        @Schema(description = "카테고리", example = "노트북")
        private String category;

        @Schema(description = "상품 가격", example = "2390000")
        private Long price;

        @Schema(description = "재고 수량", example = "10")
        private int stock;

        public ProductListResponse(Long productId, String name, String category, Long price, int stock) {
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }

        public static ProductListResponse from(ProductResult.ProductListResult result) {
            return new ProductListResponse(
                    result.productId(),
                    result.name(),
                    result.category(),
                    result.price(),
                    result.stock()
            );
        }
    }

    public static class PopularProductResponse {
        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Schema(description = "상품명", example = "MacBook Pro")
        private String name;

        @Schema(description = "가격", example = "2390000")
        private Long price;

        @Schema(description = "총 판매 수량", example = "200")
        private int totalSold;

        public PopularProductResponse(Long productId, String name, Long price, int totalSold) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.totalSold = totalSold;
        }
    }
}
