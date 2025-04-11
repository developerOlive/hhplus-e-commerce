package com.hhplusecommerce.support.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class ApiResult<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean isSuccess;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    private ApiResult(boolean isSuccess, String message, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.data = data;
    }

    /**
     * 메시지를 포함한 성공 응답
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(true, message, data);
    }

    /**
     * 기본 메시지("요청이 성공적으로 처리되었습니다.")를 사용하는 성공 응답
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }

    /**
     * 실패 응답
     */
    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(false, message, null);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
