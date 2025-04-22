package com.hhplusecommerce.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryRepository {
    Optional<ProductInventory> findInventoryByProductId(Long productId);
    List<ProductInventory> findAllByProductIdIn(List<Long> productIds);
    ProductInventory save(ProductInventory inventory);
}
