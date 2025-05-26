package com.hhplusecommerce.domain.order.event;

import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;

public interface OrderEventPublisher {
    void publish(Completed event);
}
