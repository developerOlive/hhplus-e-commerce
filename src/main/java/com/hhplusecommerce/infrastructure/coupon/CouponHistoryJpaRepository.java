package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.model.CouponHistory;
import com.hhplusecommerce.domain.coupon.type.CouponUsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponHistoryJpaRepository extends JpaRepository<CouponHistory, Long> {
    List<CouponHistory> findByUserIdAndCouponUsageStatus(Long userId, CouponUsageStatus status);
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
