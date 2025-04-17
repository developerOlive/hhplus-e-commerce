package com.hhplusecommerce.infrastructure.order;

import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time);

}
