package com.hhplusecommerce.application.external;

import com.hhplusecommerce.domain.order.OrderItems;

/**
 * 외부 데이터 플랫폼에 주문 정보를 전송하는 도메인 인터페이스
 */
public interface DataPlatformExporter {
    void sendOrder(OrderItems orderItems);
}
