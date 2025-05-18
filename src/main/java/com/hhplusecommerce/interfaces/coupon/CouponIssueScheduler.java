package com.hhplusecommerce.interfaces.coupon;

import com.hhplusecommerce.application.coupon.CouponIssueFacade;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueScheduler {

    private final CouponService couponService;
    private final CouponIssueFacade couponIssueFacade;

    @Scheduled(fixedDelay = 5000)
    public void runIssueWorker() {
        try {
            List<Long> activeCouponIds = couponService.getActiveCouponIds();

            for (Long couponId : activeCouponIds) {
                couponIssueFacade.processCouponIssues(couponId);
            }
        } catch (Exception e) {
            log.error("[CouponIssueScheduler] 쿠폰 발급 작업 실행 중 예외 발생", e);
        }
    }
}
