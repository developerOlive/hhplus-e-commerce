package com.hhplusecommerce.infrastructure.external;

import com.hhplusecommerce.application.external.DataPlatformExporter;
import com.hhplusecommerce.domain.order.OrderItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 외부 데이터 플랫폼에 주문 정보를 모의로 전송하는 Mock 구현체
 */
@Slf4j
@Component
public class MockDataPlatformExporter implements DataPlatformExporter {

    @Override
    public void sendOrder(OrderItems orderItems) {
        log.info("[Mock API] Sending order info for orderId={}", orderItems.getOrderId());
    }

    @Override
    public void sendBatchData(List<OrderItems> orderItemsList) {
        log.info("[Mock API] Sending batch order info for {} orders", orderItemsList.size());
    }
}
