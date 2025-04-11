package com.hhplusecommerce.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface CouponHistoryRepository {
    List<CouponHistory> findCouponsByUserIdAndStatus(Long userId, CouponUsageStatus available);

    CouponHistory save(CouponHistory couponHistory);

    Optional<CouponHistory> findById(Long couponId);
}
