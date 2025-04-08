package com.hhplusecommerce.support.stub;

import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * 테스트용으로 ProductRepository Stub 구현체.
 * 실제 DB와 연결되지 않고 가짜 데이터를 반환합니다.
 */
@Configuration
public class ProductRepositoryStubConfig {

    @Bean
    public ProductRepository productRepository() {
        return (criteria, pageable) -> {
            List<Product> products = List.of(
                    new Product(1L, "MacBook Pro", "노트북", 2390000L, 10),
                    new Product(2L, "iPhone 13", "스마트폰", 999000L, 50),
                    new Product(3L, "Apple Watch", "웨어러블", 499000L, 30)
            );

            return new PageImpl<>(products, pageable, products.size());
        };
    }
}
