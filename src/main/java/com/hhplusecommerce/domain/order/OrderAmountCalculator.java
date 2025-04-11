package com.hhplusecommerce.domain.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderAmountCalculator {

    /**
     * 주문 항목들의 총 금액 계산
     */
    public static BigDecimal calculateTotalAmount(List<OrderItemCommand> items) {
        return items.stream()
                .map(OrderItemCommand::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
