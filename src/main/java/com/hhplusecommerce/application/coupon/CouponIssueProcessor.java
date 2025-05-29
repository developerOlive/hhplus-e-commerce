package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 쿠폰 발급 요청을 배치로 처리하고 재고 관리 및 예외 복구를 담당
 */
@Component
@RequiredArgsConstructor
public class CouponIssueProcessor {

    private final CouponIssuePort couponIssuePort;
    private final CouponService couponService;

    public void processCouponIssues(Long couponId, int batchSize) {
        Coupon coupon = couponService.getCoupon(couponId);
        if (coupon.getIssueStatus() == CouponIssueStatus.FINISHED) {
            return;
        }

        String requestKey = couponIssuePort.getRequestQueueKey(couponId);
        String issuedKey = couponIssuePort.getIssuedKey(couponId);
        String stockKey = couponIssuePort.getStockKey(couponId);

        Set<String> poppedUsers = couponIssuePort.popRequests(requestKey, batchSize);
        if (poppedUsers.isEmpty()) {
            return;
        }
        for (String userIdStr : poppedUsers) {
            if (couponIssuePort.isIssued(issuedKey, userIdStr)) {
                continue;
            }

            Long stockLeft = couponIssuePort.decrementStock(stockKey);
            if (stockLeft == null || stockLeft < 0) {
                // 재고 부족 시 재고 복구 및 쿠폰 상태 마감 처리
                couponIssuePort.incrementStock(stockKey);
                couponService.finishCoupon(couponId);
                continue;
            }

            couponIssuePort.addIssuedUser(issuedKey, userIdStr);

            try {
                couponService.issueCoupon(new CouponCommand(Long.valueOf(userIdStr), couponId));
            } catch (Exception e) {
                // DB 발급 실패 시 발급 완료 등록 취소 및 재고 복구
                couponIssuePort.removeIssuedUser(issuedKey, userIdStr);
                couponIssuePort.incrementStock(stockKey);
                throw e;
            }

            if (stockLeft == 0) {
                couponService.finishCoupon(couponId);
            }
        }
    }
}
