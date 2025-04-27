package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.ProductInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryJpaRepository extends JpaRepository<ProductInventory, Long> {

    Optional<ProductInventory> findByProductId(Long productId);
    List<ProductInventory> findAllByProduct_IdIn(List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.product.id = :productId")
    Optional<ProductInventory> findByProductIdForUpdate(@Param("productId") Long productId);
}
