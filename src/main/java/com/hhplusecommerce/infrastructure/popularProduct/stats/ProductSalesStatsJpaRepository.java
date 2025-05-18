package com.hhplusecommerce.infrastructure.popularProduct.stats;

import com.hhplusecommerce.domain.popularProduct.model.ProductSalesStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductSalesStatsJpaRepository extends JpaRepository<ProductSalesStats, Long> {
    Optional<ProductSalesStats> findByProductIdAndSaleDate(Long productId, LocalDate date);
}
