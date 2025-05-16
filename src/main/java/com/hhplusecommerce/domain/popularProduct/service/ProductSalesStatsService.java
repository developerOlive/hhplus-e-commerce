package com.hhplusecommerce.domain.popularProduct.service;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.popularProduct.model.ProductSalesStats;
import com.hhplusecommerce.domain.popularProduct.repository.ProductSalesStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSalesStatsService {

    private final ProductSalesStatsRepository statsRepository;

    /**
     * 일자별 상품 판매 통계를 누적 기록
     */
    public void recordSales(List<OrderItem> orderItems, LocalDate saleDate) {
        for (OrderItem item : orderItems) {
            Long productId = item.getProductId();
            int quantity = item.getQuantity();
            BigDecimal amount = item.getTotalAmount();

            ProductSalesStats stats = statsRepository.findByProductIdAndSaleDate(productId, saleDate)
                    .orElseGet(() -> ProductSalesStats.initialize(productId, saleDate));

            stats.record(quantity, amount);
            statsRepository.save(stats);
        }
    }
}
