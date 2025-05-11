package com.hhplusecommerce.support.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorType errorType;

    public CustomException(ErrorType errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.status = errorType.getStatus();
        this.errorType = errorType;
    }

    public CustomException(ErrorType errorType) {
        super(errorType.getMessage());
        this.status = errorType.getStatus();
        this.errorType = errorType;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
