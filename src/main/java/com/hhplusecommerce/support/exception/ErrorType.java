package com.hhplusecommerce.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    // Product
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
    INVALID_STOCK_AMOUNT(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다."),

    // Balance
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    INVALID_BALANCE_AMOUNT(HttpStatus.BAD_REQUEST, "잔액은 0보다 커야 합니다."),
    USER_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 잔액 정보가 존재하지 않습니다."),
    INVALID_BALANCE_CHANGE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 잔액 변경 유형입니다.");


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
