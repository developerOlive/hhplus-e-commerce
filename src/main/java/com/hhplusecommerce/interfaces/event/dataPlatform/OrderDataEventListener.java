package com.hhplusecommerce.interfaces.event.dataPlatform;

import com.hhplusecommerce.application.external.DataPlatformExporter;
import com.hhplusecommerce.domain.order.event.OrderEvent.Completed;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderDataEventListener {

    private final DataPlatformExporter dataPlatformExporter;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Completed event) {
        dataPlatformExporter.sendOrder(event.orderItems());
    }
}
