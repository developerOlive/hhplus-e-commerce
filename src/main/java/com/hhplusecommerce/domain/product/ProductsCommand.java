package com.hhplusecommerce.domain.product;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductsCommand {
    private String category;
    private ProductSortOption sortOption;
    private Integer page;
    private Integer size;
}
