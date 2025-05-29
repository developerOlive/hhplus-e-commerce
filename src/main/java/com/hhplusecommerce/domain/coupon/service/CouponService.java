package com.hhplusecommerce.domain.coupon.service;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.model.CouponHistory;
import com.hhplusecommerce.domain.coupon.model.CouponResult;
import com.hhplusecommerce.domain.coupon.repository.CouponHistoryRepository;
import com.hhplusecommerce.domain.coupon.repository.CouponRepository;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.hhplusecommerce.domain.coupon.type.CouponUsageStatus.AVAILABLE;
import static com.hhplusecommerce.support.exception.ErrorType.COUPON_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponHistoryRepository couponHistoryRepository;

    /**
     * 사용 가능한 유저의 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponResult> getUserCoupons(Long userId) {
        List<CouponHistory> couponHistories = couponHistoryRepository.findCouponsByUserIdAndStatus(userId, AVAILABLE);

        return couponHistories.stream()
                .map(couponHistory -> {
                    Coupon coupon = couponHistory.getCoupon();
                    return CouponResult.from(couponHistory, coupon);
                })
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰을 유저에게 발급하고 발급 이력을 저장
     */
    @Transactional
    public Long issueCoupon(CouponCommand command) {
        int updatedRows = couponRepository.increaseIssuedQuantityIfNotExceeded(command.couponId());
        if (updatedRows == 0) {
            throw new CustomException(ErrorType.COUPON_NO_STOCK);
        }

        Coupon coupon = getCoupon(command.couponId());
        CouponHistory history = CouponHistory.issue(command.userId(), coupon);
        couponHistoryRepository.save(history);

        if (getCouponCurrentStock(command.couponId()) == 0) {
            finishCouponInternal(command.couponId());
        }

        return history.getId();
    }

    /**
     * 쿠폰 할인 금액을 계산
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long userId, Long couponIssueId, BigDecimal totalAmount) {
        if (couponIssueId == null) {
            return BigDecimal.ZERO;
        }

        CouponHistory couponHistory = getCouponHistory(userId, couponIssueId);
        Coupon coupon = couponHistory.getCoupon();
        CouponValidator.validateUsableCoupon(coupon, couponHistory);

        return coupon.discountFor(totalAmount);
    }

    /**
     * 유저의 쿠폰 이력을 조회하고, 본인 소유인지 검증
     */
    @Transactional(readOnly = true)
    public CouponHistory getCouponHistory(Long userId, Long couponIssueId) {
        CouponHistory couponHistory = couponHistoryRepository.findById(couponIssueId)
                .orElseThrow(() -> new CustomException(COUPON_NOT_FOUND));

        couponHistory.validateOwner(userId);

        return couponHistory;
    }

    /**
     * 쿠폰 사용 처리 (결제 성공 시)
     */
    @Transactional
    public void useCoupon(Order order) {
        if (!order.hasCoupon()) {
            return;
        }

        CouponHistory couponHistory = getCouponHistory(order.getUserId(), order.getCouponIssueId());
        Coupon coupon = couponHistory.getCoupon();
        CouponValidator.validateUsableCoupon(coupon, couponHistory);
        couponHistory.use();
    }

    /**
     * 쿠폰 발급 상태 완료 처리 (재고 소진)
     */
    @Transactional
    public void finishCoupon(Long couponId) {
        finishCouponInternal(couponId);
    }

    @Transactional(readOnly = true)
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorType.COUPON_NOT_FOUND));
    }

    @Transactional
    protected void finishCouponInternal(Long couponId) {
        Coupon coupon = getCoupon(couponId);

        if (coupon.getIssueStatus() == CouponIssueStatus.FINISHED) {
            return;
        }

        if (coupon.getIssuedQuantity() < coupon.getMaxQuantity()) {
            return;
        }

        coupon.completeIssue();
        couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public boolean isCouponAlreadyIssued(Long userId, Long couponId) {
        return couponHistoryRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Transactional(readOnly = true)
    public int getCouponCurrentStock(Long couponId) {
        Coupon coupon = getCoupon(couponId);
        int stock = coupon.getMaxQuantity() - coupon.getIssuedQuantity();

        return Math.max(0, stock);  // 음수 방지
    }
}
