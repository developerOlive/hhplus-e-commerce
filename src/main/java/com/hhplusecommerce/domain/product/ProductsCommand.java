package com.hhplusecommerce.domain.product;

public record ProductsCommand(
        String productName,
        Long minPrice,
        Long maxPrice,
        String category,
        Integer page,
        Integer size
) {
}
