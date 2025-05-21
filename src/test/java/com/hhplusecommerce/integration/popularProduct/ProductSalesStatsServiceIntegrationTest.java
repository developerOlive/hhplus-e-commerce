package com.hhplusecommerce.integration.popularProduct;

import com.hhplusecommerce.domain.order.*;
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

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void 상품_판매_통계를_정상적으로_기록한다() {
        // given
        Long productId = 1L;
        int quantity = 2;
        BigDecimal price = BigDecimal.valueOf(2500);
        BigDecimal expectedTotal = price.multiply(BigDecimal.valueOf(quantity));
        LocalDate saleDate = LocalDate.now();
        String category = "electronics";

        OrderItemCommand itemCommand = new OrderItemCommand(productId, quantity, price, category);
        OrderCommand orderCommand = new OrderCommand(123L, null, List.of(itemCommand));

        // Order 생성
        Order order = Order.create(orderCommand);

        // 주문 상태를 COMPLETED로 변경 (결제 완료 상태)
        order.complete();

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        // when
        statsService.recordSales(savedOrder.getOrderItems(), saleDate);

        // then
        ProductSalesStats stats = statsRepository.findByProductIdAndSaleDate(productId, saleDate)
                .orElseThrow();

        assertThat(stats.getQuantitySold()).isEqualTo(quantity);
        assertThat(stats.getTotalSalesAmount()).isEqualTo(expectedTotal);
    }
}
