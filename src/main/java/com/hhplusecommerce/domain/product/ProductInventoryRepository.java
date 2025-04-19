package com.hhplusecommerce.domain.product;

import java.util.Optional;

public interface ProductInventoryRepository {
    Optional<ProductInventory> findInventoryByProductId(Long productId);

    ProductInventory save(ProductInventory inventory);
}
