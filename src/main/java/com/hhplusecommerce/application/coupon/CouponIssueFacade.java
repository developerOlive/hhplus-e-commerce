package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueFacade {

    private final CouponIssuePort couponIssuePort;
    private final CouponKeyProvider couponKeyProvider;

    /**
     * 사용자 쿠폰 발급 요청 처리
     */
    public void requestCouponIssue(CouponCommand command) {
        Long couponId = command.couponId();
        Long userId = command.userId();

        String issuedKey = couponKeyProvider.issuedKey(couponId);
        if (couponIssuePort.isIssued(issuedKey, userId.toString())) {
            throw new CustomException(ErrorType.COUPON_ALREADY_ISSUED);
        }

        String requestKey = couponKeyProvider.requestKey(couponId);
        couponIssuePort.addToRequestQueue(requestKey, userId.toString(), System.currentTimeMillis());
    }
}
