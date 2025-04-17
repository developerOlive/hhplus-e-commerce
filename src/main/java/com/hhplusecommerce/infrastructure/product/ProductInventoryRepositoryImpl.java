package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.ProductInventory;
import com.hhplusecommerce.domain.product.ProductInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductInventoryRepositoryImpl implements ProductInventoryRepository {

    private final ProductInventoryJpaRepository productInventoryJpaRepository;

    @Override
    public Optional<ProductInventory> findInventoryByProductId(Long productId) {
        return productInventoryJpaRepository.findByProductId(productId);
    }

    @Override
    public ProductInventory save(ProductInventory inventory) {
        return productInventoryJpaRepository.save(inventory);
    }
}
