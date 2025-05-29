package com.hhplusecommerce.application.coupon.event;

public enum CouponIssueResult {
    SUCCESS("쿠폰 발급에 성공했습니다."),
    ALREADY_ISSUED("이미 발급된 쿠폰입니다."),
    OUT_OF_STOCK("쿠폰 재고가 소진되어 마감되었습니다."),
    FAILED_SYSTEM("쿠폰 발급 중 시스템 오류가 발생했습니다.");

    private final String defaultMessage;

    CouponIssueResult(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getMessage() {
        return defaultMessage;
    }
}
