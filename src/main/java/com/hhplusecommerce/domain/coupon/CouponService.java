package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 해당 쿠폰 조회
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new CustomException(ErrorType.COUPON_NOT_FOUND));

        // 발급 수량 체크 및 발급 수량 증가
        coupon.increaseIssuedQuantity();

        // 쿠폰 발급 내역 생성
        CouponHistory couponHistory = CouponHistory.issue(command.userId(), coupon);
        couponHistoryRepository.save(couponHistory);

        return couponHistory.getId();
    }
}
