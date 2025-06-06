package com.hhplusecommerce.domain.coupon.repository;

import com.hhplusecommerce.domain.coupon.model.CouponHistory;
import com.hhplusecommerce.domain.coupon.type.CouponUsageStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponHistoryRepository {
    @Query("""
        SELECT ch FROM CouponHistory ch
        JOIN FETCH ch.coupon
        WHERE ch.userId = :userId AND ch.couponUsageStatus = :status
    """)
    List<CouponHistory> findCouponsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") CouponUsageStatus status
    );

    CouponHistory save(CouponHistory couponHistory);

    Optional<CouponHistory> findById(Long couponId);

    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
