package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductInventoryService {

    private final ProductInventoryRepository productInventoryRepository;

    /**
     * 주문 항목 목록에 포함된 모든 상품의 재고를 검증
     */
    @Transactional(readOnly = true)
    public void validateAllProductStocks(List<OrderItemCommand> items) {
        for (OrderItemCommand item : items) {
            validateProductStock(item.productId(), item.quantity());
        }
    }

    /**
     * 특정 상품의 재고가 충분한지 검증
     */
    @Transactional(readOnly = true)
    public void validateProductStock(Long productId, int quantity) {
        ProductInventory inventory = productInventoryRepository.findInventoryByProductId(productId)
                .orElseThrow(() -> new CustomException(ErrorType.PRODUCT_NOT_FOUND));

        if (inventory.getStock() < quantity) {
            throw new CustomException(ErrorType.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 주문 항목 전체 재고 차감 처리
     */
    @Transactional
    public void decreaseStocks(List<OrderItem> items) {
        for (OrderItem item : items) {
            decreaseStock(item.getProductId(), item.getQuantity());
        }
    }

    /**
     * 특정 상품 재고 차감
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        ProductInventory inventory = productInventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND_PRODUCT_INVENTORY));

        if (inventory.getStock() < quantity) {
            throw new CustomException(ErrorType.INSUFFICIENT_STOCK);
        }

        inventory.decrease(quantity);
    }
}

