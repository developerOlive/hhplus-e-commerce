package com.hhplusecommerce.support.trace;

import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.domain.order.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventTraceManager {

    private final OrderEventPublisher eventPublisher;

    public void publish(OrderEvent.Completed event) {
        String traceId = MDC.get("traceId");
        String spanId = UUID.randomUUID().toString();

        event.setTraceContext(traceId, spanId);
        log.info("[EVENT_PUBLISH] traceId={}, spanId={}, eventType={}", traceId, spanId, event.getClass().getSimpleName());

        eventPublisher.publish(event);
    }
}
