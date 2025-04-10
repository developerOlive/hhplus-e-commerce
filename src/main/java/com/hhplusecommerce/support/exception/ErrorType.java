package com.hhplusecommerce.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    // Product
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
    INVALID_STOCK_AMOUNT(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다."),

    // Balance
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    INVALID_BALANCE_AMOUNT(HttpStatus.BAD_REQUEST, "잔액은 0보다 커야 합니다."),
    USER_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 잔액 정보가 존재하지 않습니다."),
    INVALID_BALANCE_CHANGE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 잔액 변경 유형입니다."),

    // Coupon
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    COUPON_INACTIVE(HttpStatus.BAD_REQUEST, "사용할 수 없는 쿠폰입니다."),
    INVALID_COUPON_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 쿠폰 할인 값입니다."),
    COUPON_ISSUE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "쿠폰 발급 수량을 초과했습니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 발급된 쿠폰입니다."),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT, "이미 사용된 쿠폰입니다."),
    UNAUTHORIZED_COUPON_ACCESS(HttpStatus.FORBIDDEN, "해당 쿠폰에 접근할 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    INVALID_ORDER_STATUS_TO_COMPLETE(HttpStatus.BAD_REQUEST, "결제 대기 상태에서만 주문을 완료할 수 있습니다."),
    INVALID_ORDER_STATUS_TO_CANCEL(HttpStatus.BAD_REQUEST, "결제 대기 상태에서만 주문을 취소할 수 있습니다."),
    INVALID_ORDER_STATUS_TO_EXPIRE(HttpStatus.BAD_REQUEST, "결제 대기 상태에서만 주문을 만료처리 할 수 있습니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액은 0 이상이어야 합니다."),
    INVALID_PAYMENT_STATUS_TO_COMPLETE(HttpStatus.BAD_REQUEST, "PENDING 상태에서 완료 처리할 수 있습니다."),
    INVALID_PAYMENT_STATUS_TO_FAIL(HttpStatus.BAD_REQUEST, "PENDING 상태에서 실패 처리할 수 있습니다.")
    ;

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
