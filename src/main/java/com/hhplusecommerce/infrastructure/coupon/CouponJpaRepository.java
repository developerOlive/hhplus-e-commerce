package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
