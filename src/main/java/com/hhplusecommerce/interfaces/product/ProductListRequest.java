package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.applicatoin.product.criteria.ProductListCriteria;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductListRequest(
        String productName,
        Long minPrice,
        Long maxPrice,
        String category,

        @NotNull
        @Min(0)
        Integer page,

        @NotNull
        @Min(1)
        Integer size
) {
    public ProductListCriteria toCriteria() {
        return new ProductListCriteria(productName, minPrice, maxPrice, category, page, size);
    }
}
