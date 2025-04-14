package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long couponIssueId;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Transient
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(Long userId, Long couponIssueId, LocalDateTime orderDate,
                  BigDecimal totalAmount, BigDecimal finalAmount, OrderStatus status) {
        if (userId == null) throw new CustomException(ErrorType.INVALID_USER_ID);
        if (orderDate == null) throw new CustomException(ErrorType.INVALID_ORDER_DATE);
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new CustomException(ErrorType.INVALID_ORDER_TOTAL_AMOUNT);
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new CustomException(ErrorType.INVALID_ORDER_FINAL_AMOUNT);
        if (status == null) throw new CustomException(ErrorType.INVALID_ORDER_STATUS);

        this.userId = userId;
        this.couponIssueId = couponIssueId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 주문 생성
     */
    public static Order create(OrderCommand command, BigDecimal totalAmount, BigDecimal finalAmount) {
        Order order = new Order(
                command.userId(),
                command.couponIssueId(),
                LocalDateTime.now(),
                totalAmount,
                finalAmount,
                OrderStatus.PAYMENT_WAIT
        );

        List<OrderItem> items = command.orderItems().stream()
                .map(item -> OrderItem.builder()
                        .orderId(null) // 아직 저장 전이라 ID 없음
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .price(item.price())
                        .build())
                .toList();

        order.orderItems.addAll(items);
        return order;
    }

    /**
     * 주문에 포함된 주문 항목 리스트 반환
     */
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    /**
     * 주문 완료 처리
     */
    public void complete() {
        if (!this.status.canComplete()) {
            throw new CustomException(ErrorType.INVALID_ORDER_STATUS_TO_COMPLETE);
        }
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 주문 만료 처리
     */
    public void expire() {
        if (!this.status.canExpire()) {
            throw new CustomException(ErrorType.INVALID_ORDER_STATUS_TO_EXPIRE);
        }
        this.status = OrderStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }
}
