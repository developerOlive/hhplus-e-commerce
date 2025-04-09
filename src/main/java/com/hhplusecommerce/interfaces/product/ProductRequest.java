package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;
import com.hhplusecommerce.domain.product.ProductsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class ProductRequest {


    @Getter
    @NoArgsConstructor
    public static class ProductSearchRequest {

        @Schema(description = "상품 이름", example = "MacBook Pro")
        private String productName;

        @Schema(description = "최소 가격", example = "100000")
        private Long minPrice;

        @Schema(description = "최대 가격", example = "3000000")
        private Long maxPrice;

        @Schema(description = "카테고리", example = "ELECTRONICS")
        private String category;

        @NotNull
        @Min(0)
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
        private Integer page;

        @NotNull
        @Min(1)
        @Schema(description = "페이지 크기", example = "20")
        private Integer size;

        public ProductsCommand toCommand() {
            return new ProductsCommand(productName, minPrice, maxPrice, category, page, size);
        }

        public Pageable toPageable() {
            return PageRequest.of(page, size);
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PopularProductSearchRequest {

        @Schema(description = "조회할 상품 개수", example = "5")
        @Min(1)
        private Integer limit = 5;

        @Schema(description = "최소 판매 수량", example = "100")
        private Integer minSold = 0;

        @Schema(description = "최근 며칠 이내 기준", example = "7")
        private Integer days = 7;

        @Schema(description = "카테고리 필터 (선택)", example = "ELECTRONICS")
        private String category;

        public PopularProductCommand toCommand() {
            return new PopularProductCommand(limit, minSold, days, category);
        }
    }
}
