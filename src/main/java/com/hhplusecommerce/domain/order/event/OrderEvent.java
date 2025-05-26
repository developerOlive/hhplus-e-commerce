package com.hhplusecommerce.domain.order.event;

import com.hhplusecommerce.domain.order.OrderItems;

public class OrderEvent {
    public record Completed(OrderItems orderItems) {}
}
