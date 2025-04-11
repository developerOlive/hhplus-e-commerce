package com.hhplusecommerce.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryRepository {
    List<ProductInventory> findInventoriesByProductIds(List<Long> productIds);
    Optional<ProductInventory> findInventoryByProductId(Long productId);
}
