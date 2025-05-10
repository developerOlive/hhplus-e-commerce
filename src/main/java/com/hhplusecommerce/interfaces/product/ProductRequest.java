package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;
import com.hhplusecommerce.domain.product.ProductsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductRequest {


    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProductSearchRequest {

        @Schema(description = "상품 이름", example = "MacBook Pro")
        private String productName;

        @Schema(description = "최소 가격", example = "100000")
        private BigDecimal minPrice;

        @Schema(description = "최대 가격", example = "3000000")
        private BigDecimal maxPrice;

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
            return ProductsCommand.builder()
                    .productName(productName != null && !productName.isBlank() ? productName : null)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .category(category != null && !category.isBlank() ? category : null)
                    .page(page)
                    .size(size)
                    .build();
        }

        public Pageable toPageable() {
            return PageRequest.of(page, size);
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PopularProductSearchRequest {

        @Schema(description = "조회할 상품 개수 (최소 1)", example = "5", defaultValue = "5")
        @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
        private Integer limit = 5;

        @Schema(description = "최소 판매 수량", example = "100", defaultValue = "100")
        private Integer minSold = 100;

        @Schema(description = "최근 며칠 기준", example = "7", defaultValue = "7")
        private Integer days = 7;

        @Schema(description = "카테고리 필터 (선택)", example = "electronics")
        private String category;

        public PopularProductCommand toCommand() {
            return new PopularProductCommand(
                    limit,
                    minSold,
                    days,
                    category,
                    LocalDate.now()
            );
        }
    }
}
