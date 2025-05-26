package com.hhplusecommerce.interfaces.event.dataPlatform;

import com.hhplusecommerce.application.external.DataPlatformExporter;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;
import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataEventListener {

    private final DataPlatformExporter dataPlatformExporter;
    private final SlackNotifier slackNotifier;

    private final List<Completed> eventBuffer = Collections.synchronizedList(new ArrayList<>());
    private final int batchSize = 100;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void bufferEvent(Completed event) {
        eventBuffer.add(event);

        if (eventBuffer.size() >= batchSize) {
            processBatch();
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void processRemainingEvents() {
        if (!eventBuffer.isEmpty()) {
            processBatch();
        }
    }

    private void processBatch() {
        List<Completed> batch;
        synchronized (eventBuffer) {
            batch = new ArrayList<>(eventBuffer);
            eventBuffer.clear();
        }

        if (!batch.isEmpty()) {
            try {
                List<OrderItems> orders = batch.stream()
                        .map(Completed::getOrderItems)
                        .collect(Collectors.toList());
                dataPlatformExporter.sendBatchData(orders);
                log.info("Successfully processed batch of {} events", batch.size());
            } catch (Exception e) {
                log.error("Failed to process batch of {} events", batch.size(), e);
                // 실패한 이벤트 개별 처리 또는 재시도 로직 추가 가능
                batch.forEach(this::retryIndividualEvent);
            }
        }
    }

    private void retryIndividualEvent(Completed event) {
        try {
            dataPlatformExporter.sendOrder(event.getOrderItems());
        } catch (Exception e) {
            log.error("[Retry Failed] orderId={}", event.getOrderItems().getOrderId(), e);
            slackNotifier.send("[Retry Failed] 주문 이벤트 처리 실패 - orderId=" + event.getOrderItems().getOrderId());
        }
    }
}
