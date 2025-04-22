package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryJpaRepository extends JpaRepository<ProductInventory, Long> {

    Optional<ProductInventory> findByProductId(Long productId);
    List<ProductInventory> findAllByProduct_IdIn(List<Long> productIds);
}
