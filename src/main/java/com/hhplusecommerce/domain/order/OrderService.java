package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

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
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Order> expiredOrders = orderRepository.findExpiredOrders(OrderStatus.PAYMENT_WAIT, oneHourAgo);

        expiredOrders.forEach(Order::expire);

        log.info("✅ [주문 만료] 1시간 초과된 결제 대기 주문 수: {}", expiredOrders.size());
    }
}
