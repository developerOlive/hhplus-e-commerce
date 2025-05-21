package com.hhplusecommerce.domain.popularProduct.service;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.popularProduct.model.ProductSalesStats;
import com.hhplusecommerce.domain.popularProduct.repository.ProductSalesStatsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProductSalesStatsService {

    private final ProductSalesStatsRepository statsRepository;

    /**
     * 일자별 상품 판매 통계를 누적 기록
     */
    public void recordSales(OrderItems orderItems, LocalDate saleDate) {
        for (OrderItem item : orderItems.getItems()) {
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
