package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 주문서에 포함된 개별 상품 항목
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderItem {

    @Id
    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;

    @Builder
    public OrderItem(Long orderId, Long productId, int quantity, BigDecimal price) {
        // 수량이 0 이하인 경우 예외 발생
        if (quantity <= 0) {
            throw new CustomException(ErrorType.INVALID_STOCK_AMOUNT);
        }

        // 가격이 0 이하인 경우 예외 발생
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }

        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = price.multiply(BigDecimal.valueOf(quantity));
    }
}
