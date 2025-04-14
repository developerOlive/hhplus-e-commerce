package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponHistoryRepository couponHistoryRepository;

    @Transactional(readOnly = true)
    public List<CouponResult> getUserCoupons(Long userId) {
        List<CouponHistory> couponHistories = couponHistoryRepository.findCouponsByUserIdAndStatus(userId, CouponUsageStatus.AVAILABLE);

        return couponHistories.stream()
                .map(couponHistory -> {
                    Coupon coupon = couponRepository.findById(couponHistory.getCouponId())
                            .orElseThrow(() -> new CustomException(ErrorType.COUPON_NOT_FOUND));

                    return CouponResult.from(couponHistory, coupon);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Long issueCoupon(CouponCommand command) {
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new CustomException(ErrorType.COUPON_NOT_FOUND));

        coupon.confirmCouponIssue();

        CouponHistory couponHistory = CouponHistory.issue(command.userId(), coupon);
        couponHistoryRepository.save(couponHistory);

        return couponHistory.getId();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long userId, Long couponIssueId, BigDecimal totalAmount) {
        if (couponIssueId == null) {
            return BigDecimal.ZERO;
        }

        CouponHistory couponHistory = getCouponHistory(userId, couponIssueId);
        Coupon coupon = getCoupon(couponHistory.getCouponId());

        CouponValidator.validateUsableCoupon(coupon, couponHistory);

        return coupon.discountFor(totalAmount);
    }

    /**
     * 유저의 쿠폰 이력을 조회하고, 본인 소유인지 검증
     */
    @Transactional(readOnly = true)
    public CouponHistory getCouponHistory(Long userId, Long couponIssueId) {
        CouponHistory couponHistory = couponHistoryRepository.findById(couponIssueId)
                .orElseThrow(() -> new CustomException(ErrorType.COUPON_NOT_FOUND));

        couponHistory.validateOwner(userId);

        return couponHistory;
    }

    /**
     * 쿠폰 정책(할인 정보 등)을 조회
     */
    @Transactional(readOnly = true)
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorType.COUPON_NOT_FOUND));
    }

    /**
     * 쿠폰 사용 처리 (결제 성공 시)
     */
    @Transactional
    public void useCoupon(Long userId, Long couponIssueId) {
        CouponHistory couponHistory = getCouponHistory(userId, couponIssueId);
        Coupon coupon = getCoupon(couponHistory.getCouponId());

        CouponValidator.validateUsableCoupon(coupon, couponHistory);

        couponHistory.use();
    }
}
