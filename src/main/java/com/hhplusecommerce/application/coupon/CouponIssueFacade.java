package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueFacade {

    private final CouponService couponService;
    private final CouponIssueProcessor couponIssueProcessor;
    private final CouponIssuePort couponIssuePort;
    private final CouponKeyProvider couponKeyProvider;

    private static final int DEFAULT_BATCH_SIZE = 10;

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

    /**
     * 쿠폰 발급 요청 처리 배치 실행
     */
    public void processCouponIssues(Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);
        if (coupon.getIssueStatus() == CouponIssueStatus.FINISHED) {
            return;
        }

        couponIssueProcessor.processCouponIssues(couponId, DEFAULT_BATCH_SIZE);
    }
}
