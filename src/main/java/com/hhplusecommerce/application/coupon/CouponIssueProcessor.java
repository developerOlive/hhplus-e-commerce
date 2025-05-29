package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import com.hhplusecommerce.application.coupon.event.CouponIssueCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.hhplusecommerce.application.coupon.event.CouponIssueResult.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueProcessor {

    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processCouponIssue(Long userId, Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);

        if (coupon.getIssueStatus() == CouponIssueStatus.FINISHED) {
            eventPublisher.publishEvent(new CouponIssueCompletedEvent(userId, couponId, OUT_OF_STOCK, OUT_OF_STOCK.getMessage()));
            throw new CustomException(ErrorType.COUPON_ALREADY_FINISHED);
        }

        if (couponService.isCouponAlreadyIssued(userId, couponId)) {
            eventPublisher.publishEvent(new CouponIssueCompletedEvent(userId, couponId, ALREADY_ISSUED, ALREADY_ISSUED.getMessage()));
            return;
        }

        try {
            couponService.issueCoupon(new CouponCommand(userId, couponId));
            eventPublisher.publishEvent(new CouponIssueCompletedEvent(userId, couponId, SUCCESS, SUCCESS.getMessage()));

            if (couponService.getCouponCurrentStock(couponId) == 0) {
                couponService.finishCoupon(couponId);
            }
        } catch (CustomException ex) {
            eventPublisher.publishEvent(new CouponIssueCompletedEvent(userId, couponId, FAILED_SYSTEM, ex.getMessage()));
            throw ex;
        } catch (Exception ex) {
            eventPublisher.publishEvent(new CouponIssueCompletedEvent(userId, couponId, FAILED_SYSTEM, FAILED_SYSTEM.getMessage()));
            throw new CustomException(ErrorType.UNKNOWN_ERROR, ex);
        }
    }
}
