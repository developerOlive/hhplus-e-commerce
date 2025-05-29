package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.port.in.CouponRequestAcceptor;
import com.hhplusecommerce.domain.coupon.port.out.CouponIssuePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 사용자 쿠폰 발급 요청 처리
 */
@Service
@RequiredArgsConstructor
public class CouponIssueFacade {

    private final CouponRequestAcceptor couponRequestAcceptor;
    private final CouponIssuePublisher couponIssuePublisher;

    public void requestCouponIssue(CouponCommand command) {
        couponRequestAcceptor.acceptCouponRequest(command);
        couponIssuePublisher.publishCouponRequest(command);
    }
}
