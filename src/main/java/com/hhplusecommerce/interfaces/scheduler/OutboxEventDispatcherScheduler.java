package com.hhplusecommerce.interfaces.scheduler;

import com.hhplusecommerce.application.outbox.OutboxEventDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Kafka로 전송되지 않은 Outbox 이벤트를 주기적으로 조회하고 발송하는 스케줄러
 */
@Component
@RequiredArgsConstructor
public class OutboxEventDispatcherScheduler {

    private final OutboxEventDispatcher outboxEventDispatcher;

    @Scheduled(fixedDelay = 5000)
    public void dispatchPendingEvents() {
        outboxEventDispatcher.dispatchPendingEvents();
    }
}
