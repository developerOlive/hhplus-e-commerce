package com.hhplusecommerce.domain.popularProduct;

public record PopularProductCommand(
        Integer limit,
        Integer minSold,
        Integer days,
        String category
) {
}
