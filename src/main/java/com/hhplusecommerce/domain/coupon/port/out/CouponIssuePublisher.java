package com.hhplusecommerce.domain.coupon.port.out;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;

/**
 * 쿠폰 발급 요청 이벤트를 외부 시스템(메시지 브로커 등)으로 발행하는 역할
 */
public interface CouponIssuePublisher {
    void publishCouponRequest(CouponCommand command);
}
