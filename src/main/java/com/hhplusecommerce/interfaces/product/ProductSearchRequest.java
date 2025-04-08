package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.domain.product.ProductsCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
