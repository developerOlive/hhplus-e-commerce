package com.hhplusecommerce.domain.coupon;

import java.util.List;

public interface CouponHistoryRepository {
    List<CouponHistory> findCouponsByUserIdAndStatus(Long userId, CouponUsageStatus available);

    void save(CouponHistory couponHistory);
}
