package com.hhplusecommerce.domain.outbox.type;

public enum OutboxStatus {

    WAITING_FOR_PUBLISH("처리 대기 중") {
        @Override
        public boolean canRetry() {
            return true; // 재시도 가능
        }
    },

    SUCCESS("처리 완료") {
        @Override
        public boolean canRetry() {
            return false; // 성공 상태는 재시도 불필요
        }
    },

    FAILED("처리 실패") {
        @Override
        public boolean canRetry() {
            return true; // 실패 시 재시도 가능
        }
    };

    private final String description;

    OutboxStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 재시도 가능한지 여부 반환
     */
    public abstract boolean canRetry();

    @Override
    public String toString() {
        return name() + "(" + description + ")";
    }
}
