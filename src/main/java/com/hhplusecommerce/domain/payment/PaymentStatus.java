package com.hhplusecommerce.domain.payment;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;

public enum PaymentStatus {

    PENDING("결제 대기 중") {
        @Override
        public PaymentStatus complete() {
            return SUCCESS;
        }

        @Override
        public PaymentStatus fail() {
            return FAILED;
        }
    },
    SUCCESS("결제 성공"),
    FAILED("결제 실패"),
    CANCELLED("결제 취소");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public PaymentStatus complete() {
        throw new CustomException(ErrorType.INVALID_PAYMENT_STATUS_TO_COMPLETE);
    }

    public PaymentStatus fail() {
        throw new CustomException(ErrorType.INVALID_PAYMENT_STATUS_TO_FAIL);
    }
}
