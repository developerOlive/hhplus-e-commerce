package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 정보 관리
 *
 * - 사용자 주문서 생성 및 상태 관리
 * - 총 금액 및 최종 결제 금액 저장
 * - 주문 완료/취소/만료 상태 처리
 */
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

    @Builder
    public Order(Long userId, Long couponIssueId, LocalDateTime orderDate, BigDecimal totalAmount, BigDecimal finalAmount, OrderStatus status) {
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
     * 주문 기한 만료 처리
     */
    public void expire() {
        if (!this.status.canExpire()) {
            throw new CustomException(ErrorType.INVALID_ORDER_STATUS_TO_EXPIRE);
        }
        this.status = OrderStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }
}
