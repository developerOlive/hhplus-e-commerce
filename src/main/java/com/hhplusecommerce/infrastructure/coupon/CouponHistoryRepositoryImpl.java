package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.model.CouponHistory;
import com.hhplusecommerce.domain.coupon.repository.CouponHistoryRepository;
import com.hhplusecommerce.domain.coupon.type.CouponUsageStatus;
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

    @Override
    public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
        return couponHistoryJpaRepository.existsByUserIdAndCouponId(userId, couponId);
    }
}
