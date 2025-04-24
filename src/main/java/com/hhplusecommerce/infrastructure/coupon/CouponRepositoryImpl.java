package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.Coupon;
import com.hhplusecommerce.domain.coupon.CouponRepository;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<Coupon> findById(Long id) {
        return couponJpaRepository.findById(id);
    }

    @Override
    public Optional<Coupon> findByIdForUpdate(Long id) {
        return couponJpaRepository.findByIdForUpdate(id);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public int issueCouponAtomically(Long couponId) {
        return couponJpaRepository.issueCouponAtomically(couponId);
    }
}
