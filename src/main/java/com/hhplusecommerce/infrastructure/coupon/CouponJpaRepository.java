package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Coupon c
        SET c.issuedQuantity = c.issuedQuantity + 1
        WHERE c.id = :couponId
        AND c.issuedQuantity < c.maxQuantity
    """)
    int increaseIssuedQuantityIfNotExceeded(@Param("couponId") Long couponId);
}
