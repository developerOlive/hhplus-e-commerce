package com.hhplusecommerce.applicatoin.product.criteria;

public record ProductListCriteria(
        String productName,
        Long minPrice,
        Long maxPrice,
        String category,
        Integer page,
        Integer size
) {}
