package com.hhplusecommerce.domain.popularProduct;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductSalesStatsRepository {
    void save(ProductSalesStats stats);

    Optional<ProductSalesStats> findByProductIdAndSaleDate(Long productId, LocalDate date);
}
