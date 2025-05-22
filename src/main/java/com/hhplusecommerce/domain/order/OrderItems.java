package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;

import java.math.BigDecimal;
import java.util.List;

import static com.hhplusecommerce.support.exception.ErrorType.EMPTY_ORDER_ITEMS;

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

    public Long getOrderId() {
        if (items.isEmpty()) {
            throw new CustomException(EMPTY_ORDER_ITEMS);
        }

        return items.get(0).getOrder().getId();
    }
}
