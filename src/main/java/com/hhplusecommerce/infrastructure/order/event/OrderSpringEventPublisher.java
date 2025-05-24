package com.hhplusecommerce.infrastructure.order.event;

import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;
import com.hhplusecommerce.domain.order.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSpringEventPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(Completed event) {
        applicationEventPublisher.publishEvent(event);
    }
}
