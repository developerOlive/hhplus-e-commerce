package com.hhplusecommerce.applicatoin.product;

import com.hhplusecommerce.domain.product.Product;

public record ProductsResult(
        Long productId,
        String name,
        String category,
        Long price,
        int stock
) {
    public static ProductsResult of(Product product, int stock) {
        return new ProductsResult(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                stock
        );
    }
}
