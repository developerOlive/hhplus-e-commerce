package com.hhplusecommerce.domain.order;

import java.util.List;

public interface OrderItemRepository {
    List<OrderItem> findByOrderId(Long orderId);
}
