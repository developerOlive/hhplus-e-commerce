package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 상품 재고 도메인
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_inventory")
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    private int stock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public ProductInventory(Product product, int stock) {
        if (product == null) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_ID);
        }
        if (stock < 0) {
            throw new CustomException(ErrorType.INVALID_STOCK_AMOUNT);
        }
        this.product = product;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void increase(int amount) {
        validateAmount(amount);
        stock += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrease(int amount) {
        validateAmount(amount);
        if (amount > stock) {
            throw new CustomException(ErrorType.INSUFFICIENT_STOCK);
        }
        stock -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorType.INVALID_STOCK_AMOUNT);
        }
    }

    private int validatePositive(int stock) {
        if (stock < 0) {
            throw new CustomException(ErrorType.INVALID_STOCK_AMOUNT);
        }
        return stock;
    }
}
