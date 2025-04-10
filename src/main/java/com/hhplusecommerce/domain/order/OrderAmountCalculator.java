package com.hhplusecommerce.domain.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderAmountCalculator {

    /**
     * 주문 항목들의 총 금액을 계산합니다.
     *
     * @param items 주문 항목 리스트
     * @return 총 결제 전 금액
     */
    public static BigDecimal calculateTotalAmount(List<OrderItemCommand> items) {
        return items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
