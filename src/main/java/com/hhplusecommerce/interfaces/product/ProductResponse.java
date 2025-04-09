package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.domain.popularProduct.PopularProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ProductResponse {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductsResponse {
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

        public static PopularProductResponse from(PopularProduct product) {
            return new PopularProductResponse(
                    product.getProductId(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getTotalSold()
            );
        }
    }
}
