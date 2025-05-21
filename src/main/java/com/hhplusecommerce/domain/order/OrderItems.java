package com.hhplusecommerce.domain.order;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 항목(OrderItem) 컬렉션을 캡슐화하여 관련 도메인 로직을 위임하는 일급 컬렉션
 */

public class OrderItems {

    private final List<OrderItem> items;

    public OrderItems(List<OrderItem> items) {
        this.items = List.copyOf(items);
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal totalAmount() {
        return items.stream()
                .map(OrderItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }
}
