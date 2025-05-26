package com.hhplusecommerce.application.external;

import com.hhplusecommerce.domain.order.OrderItems;

import java.util.List;

/**
 * 외부 데이터 플랫폼에 주문 정보를 전송하는 도메인 인터페이스
 */
public interface DataPlatformExporter {
    void sendOrder(OrderItems orderItems);


    // 여러 주문 건을 한꺼번에 전송
    void sendBatchData(List<OrderItems> orderItemsList);
}
