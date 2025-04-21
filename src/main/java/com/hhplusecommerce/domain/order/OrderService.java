package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 주문 생성 (결제 전 상태)
     */
    @Transactional
    public Long createOrder(OrderCommand command, BigDecimal discountAmount) {
        Order order = Order.create(command);
        BigDecimal finalAmount = order.getTotalAmount().subtract(discountAmount).max(BigDecimal.ZERO);
        order.applyFinalAmount(finalAmount);

        orderRepository.save(order);

        return order.getId();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new CustomException(ErrorType.ORDER_NOT_FOUND));
    }

    /**
     * 주문 상세 항목 조회
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    /**
     * 주문 완료 처리
     */
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorType.ORDER_NOT_FOUND));
        order.complete();
    }

    /**
     * 주문 만료 처리
     */
    @Transactional
    public void expireOverdueOrders() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PAYMENT_WAIT, thirtyMinutesAgo);

        for (Order order : expiredOrders) {
            order.expire();
        }
    }
}
