package com.hhplusecommerce.infrastructure.order;

import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = :orderStatus AND o.createdAt <= :expiredBefore")
    List<Order> findExpiredOrders(@Param("orderStatus") OrderStatus orderStatus,
                                  @Param("expiredBefore") LocalDateTime expiredBefore);
}
