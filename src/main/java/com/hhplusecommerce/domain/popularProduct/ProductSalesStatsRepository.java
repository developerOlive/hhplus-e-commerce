package com.hhplusecommerce.domain.popularProduct;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductSalesStatsRepository {
    Optional<ProductSalesStats> findByProductIdAndDate(Long productId, LocalDate date);
    void save(ProductSalesStats stats);
}
