package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.domain.coupon.CouponService;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "`order`")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long couponIssueId;
    private LocalDateTime orderDate;
    @Column(precision = 10, scale = 0)
    private BigDecimal totalAmount;
    @Column(precision = 10, scale = 0)
    private BigDecimal finalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(Long userId, Long couponIssueId, LocalDateTime orderDate,
                  BigDecimal totalAmount, BigDecimal finalAmount, OrderStatus orderStatus) {
        if (userId == null) {
            throw new CustomException(ErrorType.INVALID_USER_ID);
        }

        if (orderDate == null) {
            throw new CustomException(ErrorType.INVALID_ORDER_DATE);
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_ORDER_TOTAL_AMOUNT);
        }

        if (orderStatus == null) {
            throw new CustomException(ErrorType.INVALID_ORDER_STATUS);
        }

        this.userId = userId;
        this.couponIssueId = couponIssueId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.orderStatus = orderStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Order create(OrderCommand command) {
        if (command.userId() == null) {
            throw new CustomException(ErrorType.INVALID_USER_ID);
        }

        List<OrderItem> items = command.orderItems().stream()
                .map(item -> new OrderItem(
                        null,
                        item.productId(),
                        item.quantity(),
                        item.price(),
                        item.category()
                ))
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(
                command.userId(),
                command.couponIssueId(),
                LocalDateTime.now(),
                totalAmount,
                null, // 최종금액은 외부에서 할인 적용 후 지정
                OrderStatus.PAYMENT_WAIT
        );

        items.forEach(item -> item.setOrder(order));
        order.orderItems.addAll(items);

        return order;
    }

    public void applyFinalAmount(BigDecimal finalAmount) {
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_ORDER_FINAL_AMOUNT);
        }
        this.finalAmount = finalAmount;
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
        if (!this.orderStatus.canComplete()) {
            throw new CustomException(ErrorType.INVALID_ORDER_STATUS_TO_COMPLETE);
        }
        this.orderStatus = OrderStatus.COMPLETED;
    }

    /**
     * 주문 만료 처리
     */
    public void expire() {
        if (!this.orderStatus.canExpire()) {
            throw new CustomException(ErrorType.INVALID_ORDER_STATUS_TO_EXPIRE);
        }
        this.orderStatus = OrderStatus.EXPIRED;
    }

    public boolean hasCoupon() {
        return this.couponIssueId != null;
    }

    /**
     * 쿠폰이 있으면 쿠폰 사용 처리 호출
     */
    public void applyCouponIfPresent(Long userId, CouponService couponService) {
        if (this.hasCoupon()) {
            couponService.useCoupon(userId, this.couponIssueId);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
