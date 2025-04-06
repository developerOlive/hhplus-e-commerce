package com.hhplusecommerce.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    // Product
    INSUFFICIENT_INVENTORY(HttpStatus.BAD_REQUEST, "Not enough stock available for this product."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "Invalid quantity specified. Quantity must be positive.");

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
