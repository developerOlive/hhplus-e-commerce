package com.hhplusecommerce.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    // Product
    INSUFFICIENT_INVENTORY(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다.");

    private final HttpStatus status;
    private final String message;

    ErrorType(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
