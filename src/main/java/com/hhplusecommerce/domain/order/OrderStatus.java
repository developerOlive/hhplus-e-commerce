package com.hhplusecommerce.domain.order;

public enum OrderStatus {
    PAYMENT_WAIT("주문서 작성 후 결제 대기"),
    COMPLETED("주문 최종 완료"),
    CANCELED("주문 취소"),
    EXPIRED("주문 기한 만료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
