package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.CouponHistory;
import com.hhplusecommerce.domain.coupon.CouponHistoryRepository;
import com.hhplusecommerce.domain.coupon.CouponUsageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponHistoryRepositoryImpl implements CouponHistoryRepository {

    private final CouponHistoryJpaRepository couponHistoryJpaRepository;

    @Override
    public List<CouponHistory> findCouponsByUserIdAndStatus(Long userId, CouponUsageStatus status) {
        return couponHistoryJpaRepository.findByUserIdAndCouponUsageStatus(userId, status);
    }

    @Override
    public Optional<CouponHistory> findById(Long couponHistoryId) {
        return couponHistoryJpaRepository.findById(couponHistoryId);
    }

    @Override
    public CouponHistory save(CouponHistory couponHistory) {
        return couponHistoryJpaRepository.save(couponHistory);
    }
}
