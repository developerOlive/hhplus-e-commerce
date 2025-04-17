package com.hhplusecommerce.domain.product;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

public enum ProductSortOption {
    LATEST(new OrderSpecifier<>(Order.DESC, QProduct.product.createdAt)),
    PRICE_ASC(new OrderSpecifier<>(Order.ASC, QProduct.product.price)),
    PRICE_DESC(new OrderSpecifier<>(Order.DESC, QProduct.product.price));

    private final OrderSpecifier<?> orderSpecifier;

    ProductSortOption(OrderSpecifier<?> orderSpecifier) {
        this.orderSpecifier = orderSpecifier;
    }

    public OrderSpecifier<?> getOrderSpecifier() {
        return orderSpecifier;
    }
}
