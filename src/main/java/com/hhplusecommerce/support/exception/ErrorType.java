package com.hhplusecommerce.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    // Product
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
    INVALID_STOCK_AMOUNT(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다."),
    INVALID_PRODUCT_ID(HttpStatus.NOT_FOUND, "상품 ID를 확인해 주세요"),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "상품 이름은 필수입니다."),
    INVALID_PRODUCT_CATEGORY(HttpStatus.BAD_REQUEST, "상품 카테고리는 필수입니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "상품 가격은 0 이상이어야 합니다."),
    INVALID_TOTAL_SOLD_COUNT(HttpStatus.BAD_REQUEST, "판매량은 0 이상이어야 합니다."),
    INVALID_SALE_DATE(HttpStatus.BAD_REQUEST, "판매 일자가 유효하지 않습니다."),
    INVALID_SALES_AMOUNT(HttpStatus.BAD_REQUEST, "판매 금액이 유효하지 않습니다."),
    NOT_FOUND_PRODUCT_INVENTORY(HttpStatus.NOT_FOUND, "해당 상품의 재고 정보를 찾을 수 없습니다."),

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
    INVALID_COUPON_ISSUE_DATA(HttpStatus.BAD_REQUEST, "쿠폰 발급 이력 생성 정보가 유효하지 않습니다."),
    INVALID_COUPON_STATUS(HttpStatus.BAD_REQUEST, "쿠폰 상태 정보가 누락되었거나 잘못되었습니다."),
    INVALID_COUPON_NAME(HttpStatus.BAD_REQUEST, "쿠폰 이름은 필수입니다."),
    INVALID_COUPON_DISCOUNT_TYPE(HttpStatus.BAD_REQUEST, "할인 타입이 지정되지 않았습니다."),
    INVALID_COUPON_QUANTITY(HttpStatus.BAD_REQUEST, "쿠폰 발급 수량은 1 이상이어야 합니다."),
    INVALID_COUPON_DATE_RANGE(HttpStatus.BAD_REQUEST, "쿠폰 유효 기간이 올바르지 않습니다."),
    INVALID_COUPON_TYPE(HttpStatus.BAD_REQUEST, "쿠폰 타입이 지정되지 않았습니다."),
    COUPON_ISSUE_FAILED(HttpStatus.BAD_REQUEST, "쿠폰 발급에 실패했습니다."),
    COUPON_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "이미 발급이 마감된 쿠폰입니다."),
    COUPON_NO_STOCK(HttpStatus.CONFLICT, "쿠폰 재고가 부족합니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    INVALID_ORDER_STATUS_TO_COMPLETE(HttpStatus.BAD_REQUEST, "결제 대기 상태에서만 주문을 완료할 수 있습니다."),
    INVALID_ORDER_STATUS_TO_CANCEL(HttpStatus.BAD_REQUEST, "결제 대기 상태에서만 주문을 취소할 수 있습니다."),
    INVALID_ORDER_STATUS_TO_EXPIRE(HttpStatus.BAD_REQUEST, "결제 대기 상태에서만 주문을 만료처리 할 수 있습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "잘못된 사용자 ID입니다."),
    INVALID_ORDER_DATE(HttpStatus.BAD_REQUEST, "주문 날짜가 유효하지 않습니다."),
    INVALID_ORDER_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST, "총 주문 금액이 잘못되었습니다."),
    INVALID_ORDER_FINAL_AMOUNT(HttpStatus.BAD_REQUEST, "최종 결제 금액이 잘못되었습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "주문 상태가 유효하지 않습니다."),
    EMPTY_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "주문 항목이 비어 있습니다. orderId를 가져올 수 없습니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액은 0 이상이어야 합니다."),
    INVALID_PAYMENT_STATUS_TO_COMPLETE(HttpStatus.BAD_REQUEST, "PENDING 상태에서 완료 처리할 수 있습니다."),
    INVALID_PAYMENT_STATUS_TO_FAIL(HttpStatus.BAD_REQUEST, "PENDING 상태에서 실패 처리할 수 있습니다."),
    INVALID_PAYMENT_ORDER_ID(HttpStatus.BAD_REQUEST, "결제에 필요한 주문 ID가 누락되었습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "결제 상태 정보가 누락되었거나 잘못되었습니다."),

    // Concurrency
    CONCURRENT_TRANSACTION_EXCEPTION(HttpStatus.CONFLICT, "동시성 오류가 발생했습니다. 다시 시도해 주세요."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "락을 획득할 수 없습니다."),

    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 오류가 발생했습니다.");

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
