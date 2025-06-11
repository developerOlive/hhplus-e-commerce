package com.hhplusecommerce.domain.coupon.repository;

import com.hhplusecommerce.domain.coupon.model.Coupon;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findById(@Param("id") Long id);
    Coupon save(Coupon coupon);
    int increaseIssuedQuantityIfNotExceeded(Long couponId);
}
