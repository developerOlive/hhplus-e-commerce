package com.hhplusecommerce.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findById(Long id);
}
