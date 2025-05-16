package com.hhplusecommerce.domain.order;

import java.math.BigDecimal;

public record OrderItemCommand(
        Long productId,
        int quantity,
        BigDecimal price,
        String category
) {
    public BigDecimal totalAmount() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
