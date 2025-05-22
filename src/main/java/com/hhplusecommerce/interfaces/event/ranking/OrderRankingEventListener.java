package com.hhplusecommerce.interfaces.event.ranking;

import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;
import com.hhplusecommerce.domain.popularProduct.service.PopularProductRankingService;
import com.hhplusecommerce.domain.popularProduct.service.ProductSalesStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OrderRankingEventListener {

    private final ProductSalesStatsService productSalesStatsService;
    private final PopularProductRankingService popularProductRankingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Completed event) {
        productSalesStatsService.recordSales(event.orderItems(), LocalDate.now());
        popularProductRankingService.recordSales(event.orderItems());
    }
}
