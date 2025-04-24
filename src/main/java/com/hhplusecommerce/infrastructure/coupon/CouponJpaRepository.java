package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :couponId")
    Optional<Coupon> findByIdForUpdate(@Param("couponId") Long couponId);

    @Modifying
    @Query("""
        UPDATE Coupon c
        SET c.issuedQuantity = c.issuedQuantity + 1
        WHERE c.id = :couponId AND c.issuedQuantity < c.maxQuantity
    """)
    int issueCouponAtomically(@Param("couponId") Long couponId);
}
