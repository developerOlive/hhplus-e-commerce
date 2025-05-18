package com.hhplusecommerce.infrastructure.popularProduct.stats;

import com.hhplusecommerce.domain.popularProduct.model.ProductSalesStats;
import com.hhplusecommerce.domain.popularProduct.repository.ProductSalesStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductSalesStatsRepositoryImpl implements ProductSalesStatsRepository {

    private final ProductSalesStatsJpaRepository productSalesStatsJpaRepository;

    @Override
    public void save(ProductSalesStats stats) {
        productSalesStatsJpaRepository.save(stats);
    }

    @Override
    public Optional<ProductSalesStats> findByProductIdAndSaleDate(Long productId, LocalDate date) {
        return productSalesStatsJpaRepository.findByProductIdAndSaleDate(productId, date);
    }
}
