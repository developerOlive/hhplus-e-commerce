package com.hhplusecommerce.integration.popularProduct;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.popularProduct.model.ProductSalesStats;
import com.hhplusecommerce.domain.popularProduct.repository.ProductSalesStatsRepository;
import com.hhplusecommerce.domain.popularProduct.service.ProductSalesStatsService;
import com.hhplusecommerce.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
class ProductSalesStatsServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ProductSalesStatsService statsService;

    @Autowired
    private ProductSalesStatsRepository statsRepository;

    @Test
    void 상품_판매_통계를_정상적으로_기록한다() {
        // given
        Long productId = 1L;
        int quantity = 2;
        BigDecimal price = BigDecimal.valueOf(2500);
        BigDecimal expectedTotal = price.multiply(BigDecimal.valueOf(quantity));
        LocalDate saleDate = LocalDate.now();
        String category = "electronics";

        OrderItem item = new OrderItem(null, productId, quantity, price, category);
        List<OrderItem> orderItems = List.of(item);

        // when
        statsService.recordSales(orderItems, saleDate);

        // then
        ProductSalesStats stats = statsRepository.findByProductIdAndSaleDate(productId, saleDate)
                .orElseThrow();

        assertThat(stats.getQuantitySold()).isEqualTo(quantity);
        assertThat(stats.getTotalSalesAmount()).isEqualTo(expectedTotal);
    }
}
