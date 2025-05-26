package com.hhplusecommerce.interfaces.event.dataPlatform;

import com.hhplusecommerce.application.external.DataPlatformExporter;
import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;
import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataEventListener {

    private final DataPlatformExporter dataPlatformExporter;
    private final SlackNotifier slackNotifier;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Completed event) {
        MDC.put("traceId", event.getTraceId());
        MDC.put("spanId", event.getSpanId());
        Long orderId = event.getOrderItems().getOrderId();

        try {
            dataPlatformExporter.sendOrder(event.getOrderItems());
        } catch (Exception e) {
            log.error("[EVENT ERROR] [OrderDataEventListener] 데이터 플랫폼 전송 실패 - orderId={}", orderId, e);
            slackNotifier.send("[EVENT ERROR] 주문 이벤트 처리 실패 - orderId=" + orderId);
        } finally {
            MDC.clear();
        }
    }
}
