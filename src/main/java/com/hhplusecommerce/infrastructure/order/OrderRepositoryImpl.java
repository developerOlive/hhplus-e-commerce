package com.hhplusecommerce.infrastructure.order;

import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderRepository;
import com.hhplusecommerce.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Optional<Order> findByIdWithItems(Long orderId) {
        return orderJpaRepository.findByIdWithItems(orderId);
    }

    @Override
    public List<Order> findExpiredOrders(OrderStatus status, LocalDateTime expiredBefore) {
        return orderJpaRepository.findExpiredOrders(status, expiredBefore);
    }
}
