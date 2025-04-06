package com.hhplusecommerce.support.config;

import com.hhplusecommerce.domain.product.ProductRepository;
import com.hhplusecommerce.domain.inventory.ProductInventoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class StubRepositoryConfig {

    @Bean
    public ProductRepository productRepository() {
        return criteria -> List.of(); // 테스트용 빈 리스트
    }

    @Bean
    public ProductInventoryRepository productInventoryRepository() {
        return productIds -> Map.of(); // 테스트용 빈 맵
    }
}
