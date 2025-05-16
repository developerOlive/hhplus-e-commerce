package com.hhplusecommerce.infrastructure.coupon;

import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public int increaseIssuedQuantityIfNotExceeded(Long couponId) {
        return couponJpaRepository.increaseIssuedQuantityIfNotExceeded(couponId);
    }

    @Override
    public List<Long> findActiveCouponIds() {
        return couponJpaRepository.findActiveCouponIds();
    }
}
