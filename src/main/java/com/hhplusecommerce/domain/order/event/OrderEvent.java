package com.hhplusecommerce.domain.order.event;

import com.hhplusecommerce.domain.order.OrderItems;

/**
 * 주문 도메인 이벤트 정의
 */
public class OrderEvent {

    public static class Completed {
        private final OrderItems orderItems;
        private String traceId;
        private String spanId;

        public Completed(OrderItems orderItems) {
            this.orderItems = orderItems;
        }

        public OrderItems getOrderItems() {
            return orderItems;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getSpanId() {
            return spanId;
        }

        public void setTraceContext(String traceId, String spanId) {
            this.traceId = traceId;
            this.spanId = spanId;
        }

        public String aggregateType() {
            return "order";
        }

        public String aggregateId() {
            return String.valueOf(orderItems.getOrderId());
        }
    }
}
