package com.hhplusecommerce.domain.product;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductsCommand {
    private final String productName;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String category;
    private final Integer page;
    private final Integer size;
}
