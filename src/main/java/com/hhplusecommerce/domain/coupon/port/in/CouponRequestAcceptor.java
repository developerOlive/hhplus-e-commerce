package com.hhplusecommerce.domain.coupon.port.in;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;

/**
 * 쿠폰 발급 요청 수락 및 초기 과정 처리 인터페이스
 */
public interface CouponRequestAcceptor {
    void acceptCouponRequest(CouponCommand command);
}
