package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 주문서에 포함된 개별 상품 항목
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderItem(Order order, Long productId, int quantity, BigDecimal price) {
        if (quantity <= 0) {
            throw new CustomException(ErrorType.INVALID_STOCK_AMOUNT);
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }

        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = price.multiply(BigDecimal.valueOf(quantity));
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
