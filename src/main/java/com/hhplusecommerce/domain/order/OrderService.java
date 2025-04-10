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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 주문 생성 (결제 전 상태)
     */
    @Transactional
    public Long createOrder(OrderCommand command, BigDecimal totalAmount, BigDecimal finalAmount) {
        Order order = Order.builder()
                .userId(command.userId())
                .couponIssueId(command.couponIssueId())
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .finalAmount(finalAmount)
                .status(OrderStatus.PAYMENT_WAIT)
                .build();

        orderRepository.save(order);

        List<OrderItem> orderItems = command.orderItems().stream()
                .map(item -> OrderItem.builder()
                        .orderId(order.getId())
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .price(item.price())
                        .build())
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        return order.getId();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
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
        order.completeOrder();
    }

    /**
     * 주문 취소 처리
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorType.ORDER_NOT_FOUND));
        order.cancelOrder();
    }

    /**
     * 주문 만료 처리
     */
    @Transactional
    public void updateExpiredOrders() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PAYMENT_WAIT, thirtyMinutesAgo);

        for (Order order : expiredOrders) {
            order.expireOrder();
        }
    }
}
