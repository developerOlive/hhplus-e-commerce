package com.hhplusecommerce.interfaces.event.ranking;

import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;
import com.hhplusecommerce.domain.popularProduct.service.PopularProductRankingService;
import com.hhplusecommerce.domain.popularProduct.service.ProductSalesStatsService;
import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRankingEventListener {

    private final ProductSalesStatsService productSalesStatsService;
    private final PopularProductRankingService popularProductRankingService;
    private final SlackNotifier slackNotifier;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Completed event) {
        MDC.put("traceId", event.getTraceId());
        MDC.put("spanId", event.getSpanId());

        Long orderId = event.getOrderItems().getOrderId();

        try {
            productSalesStatsService.recordSales(event.getOrderItems(), LocalDate.now());
        } catch (Exception e) {
            log.error("[EVENT ERROR] [OrderRankingEventListener] 상품 통계 저장 실패 - orderId={}", orderId, e);
            slackNotifier.send("[EVENT ERROR] 상품 통계 저장 실패 - orderId=" + orderId);
        }

        try {
            popularProductRankingService.recordSales(event.getOrderItems());
        } catch (Exception e) {
            log.error("[EVENT ERROR] [OrderRankingEventListener] 인기상품 랭킹 반영 실패 - orderId={}", orderId, e);
            slackNotifier.send("[EVENT ERROR] 랭킹 반영 실패 - orderId=" + orderId);
        } finally {
            MDC.clear();
        }
    }
}
