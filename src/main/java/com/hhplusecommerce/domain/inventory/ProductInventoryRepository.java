package com.hhplusecommerce.domain.inventory;

import java.util.List;
import java.util.Map;

public interface ProductInventoryRepository {
    Map<Long, ProductInventory> getInventoryByProductIds(List<Long> productIds);
}
