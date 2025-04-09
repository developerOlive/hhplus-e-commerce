package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 재고 도메인
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductInventory {

    @Id
    private Long productId;

    private int stock;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public ProductInventory(Long productId, int stock) {
        this.productId = productId;
        this.stock = validateInitialStock(stock);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    /**
     * 재고 증가
     * @param amount 증가할 수량 (양수)
     */
    public void increaseStock(int amount) {
        validateAmount(amount);

        stock += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고를 차감
     * @param amount 차감할 수량 (양수)
     */
    public void decreaseStock(int amount) {
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

    private int validateInitialStock(int stock) {
        if (stock < 0) {
            throw new CustomException(ErrorType.INVALID_STOCK_AMOUNT);
        }
        return stock;
    }
}
