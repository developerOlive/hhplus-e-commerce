package com.hhplusecommerce.domain.product;

import java.math.BigDecimal;

public record ProductDataResult(
        Long productId,
        String name,
        String category,
        BigDecimal price
) {
    public static ProductDataResult from(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDataResult(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice()
        );
    }
}
