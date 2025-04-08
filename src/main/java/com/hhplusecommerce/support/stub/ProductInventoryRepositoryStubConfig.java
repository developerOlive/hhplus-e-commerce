package com.hhplusecommerce.support.stub;

import com.hhplusecommerce.domain.inventory.ProductInventory;
import com.hhplusecommerce.domain.inventory.ProductInventoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트용으로 ProductInventoryRepository Stub 구현체.
 * 실제 DB와 연결되지 않고 가짜 데이터를 반환합니다.
 */
@Configuration
public class ProductInventoryRepositoryStubConfig {

    @Bean
    public ProductInventoryRepository productInventoryRepository() {
        return new ProductInventoryRepository() {
            @Override
            public Map<Long, ProductInventory> getInventoryByProductIds(List<Long> productIds) {
                Map<Long, ProductInventory> inventoryData = new HashMap<>();
                inventoryData.put(1L, new ProductInventory(1L, 100));  // 상품 1의 재고 100
                inventoryData.put(2L, new ProductInventory(2L, 50));   // 상품 2의 재고 50
                inventoryData.put(3L, new ProductInventory(3L, 30));   // 상품 3의 재고 30
                return inventoryData;
            }
        };
    }
}
