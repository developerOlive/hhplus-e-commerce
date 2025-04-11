package com.hhplusecommerce.domain.popularProduct;

import com.hhplusecommerce.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSalesStatsService {

    private final ProductSalesStatsRepository statsRepository;

    public void recordSales(List<OrderItem> orderItems, LocalDate saleDate) {
        for (OrderItem item : orderItems) {
            Long productId = item.getProductId();
            int quantity = item.getQuantity();
            BigDecimal amount = item.getTotalAmount();

            ProductSalesStats stats = statsRepository.findByProductIdAndDate(productId, saleDate)
                    .orElseGet(() -> ProductSalesStats.initialize(productId, saleDate));

            stats.record(quantity, amount);
            statsRepository.save(stats);
        }
    }
}
