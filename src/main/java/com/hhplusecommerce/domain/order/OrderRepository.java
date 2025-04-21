package com.hhplusecommerce.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    Optional<Order> findByIdWithItems(Long orderId);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus paymentWait, LocalDateTime thirtyMinutesAgo);
}
