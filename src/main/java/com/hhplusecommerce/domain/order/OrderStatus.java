package com.hhplusecommerce.domain.order;

public enum OrderStatus {
    PAYMENT_WAIT("주문서 작성 후 결제 대기") {
        @Override
        public boolean canComplete() {
            return true;
        }

        @Override
        public boolean canExpire() {
            return true;
        }
    },
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

    /**
     * 현재 상태(OrderStatus)가 '주문 완료' 상태로 변경될 수 있는 상태인지 여부를 반환
     * 기본적으로는 불가능하며, PAYMENT_WAIT 상태에서만 true 반환
     */
    public boolean canComplete() {
        return false;
    }

    /**
     * 현재 상태(OrderStatus)가 '주문 만료' 상태로 변경될 수 있는 상태인지 여부를 반환
     * 기본적으로는 불가능하며, PAYMENT_WAIT 상태에서만 true 반환
     */
    public boolean canExpire() {
        return false;
    }
}
