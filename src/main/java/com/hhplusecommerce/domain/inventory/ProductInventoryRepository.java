package com.hhplusecommerce.domain.inventory;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface ProductInventoryRepository {
    Map<Long, ProductInventory> getInventoryByProductIds(List<Long> productIds);
}
