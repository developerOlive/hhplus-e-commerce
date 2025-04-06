package com.hhplusecommerce.applicatoin.product.result;

public class ProductResult {

    public record ProductListResult(
            Long productId,
            String name,
            String category,
            Long price,
            int stock
    ) {
    }

    public record PopularProductResult(
            Long productId,
            String name,
            Long price,
            int totalSold
    ) {
    }
}
