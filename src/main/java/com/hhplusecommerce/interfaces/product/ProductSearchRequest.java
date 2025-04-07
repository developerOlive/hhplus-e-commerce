package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.domain.product.ProductsCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductSearchRequest(
        String productName,
        Long minPrice,
        Long maxPrice,
        String category,
        @NotNull @Min(0) Integer page,
        @NotNull @Min(1) Integer size
) {
    public ProductsCommand toCommand() {
        return new ProductsCommand(productName, minPrice, maxPrice, category, page, size);
    }
}
