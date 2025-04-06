package com.hhplusecommerce.domain.inventory;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;

/**
 * 상품 재고 도메인
 */
public class ProductInventory {

    private final Long productId;
    private int quantity;

    public ProductInventory(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = Math.max(quantity, 0); // 음수로 세팅 방지
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int amount) {
        validateAmount(amount);
        quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        validateAmount(amount);
        if (amount > quantity) {
            throw new CustomException(ErrorType.INSUFFICIENT_INVENTORY);
        }
        quantity -= amount;
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorType.INVALID_QUANTITY);
        }
    }
}
