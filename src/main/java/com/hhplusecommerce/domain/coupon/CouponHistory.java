package com.hhplusecommerce.domain.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class CouponHistory {

    @Id
    private Long id;
    private Long userId;
    private Long couponId;
    private LocalDate publishDate;
    private LocalDate useDate;
    private CouponUsageStatus couponUsageStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CouponHistory issue(Long userId, Coupon coupon) {
        return new CouponHistory(
                null,
                userId,
                coupon.getId(),
                LocalDate.now(),
                null,
                CouponUsageStatus.AVAILABLE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public void markUsed() {
        this.couponUsageStatus = CouponUsageStatus.USED;
        this.useDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }
}
