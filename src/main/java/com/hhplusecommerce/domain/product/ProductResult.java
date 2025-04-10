package com.hhplusecommerce.domain.product;

import java.math.BigDecimal;

public record ProductResult(
        Long productId,
        String name,
        String category,
        BigDecimal price,
        int stock
) {
    public static ProductResult from(Product product, int stock) {
        return new ProductResult(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                stock
        );
    }
}
