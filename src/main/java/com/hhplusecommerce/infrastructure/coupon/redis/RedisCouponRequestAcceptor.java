package com.hhplusecommerce.infrastructure.coupon.redis;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.domain.coupon.port.in.CouponRequestAcceptor;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 요청 중복 확인 및 선착순 큐 등록을 처리하는 Redis 구현체
 */
@Service
@RequiredArgsConstructor
public class RedisCouponRequestAcceptor implements CouponRequestAcceptor {

    private final CouponIssuePort couponIssuePort;
    private final CouponKeyProvider couponKeyProvider;

    @Override
    public void acceptCouponRequest(CouponCommand command) {
        Long couponId = command.couponId();
        Long userId = command.userId();
        String issuedKey = couponKeyProvider.issuedKey(couponId);

        // 중복 발급 검증
        if (couponIssuePort.isIssued(issuedKey, userId.toString())) {
            throw new CustomException(ErrorType.COUPON_ALREADY_ISSUED);
        }

        String requestKey = couponKeyProvider.requestKey(couponId);
        couponIssuePort.addToRequestQueue(requestKey, userId.toString(), System.currentTimeMillis());
    }
}
