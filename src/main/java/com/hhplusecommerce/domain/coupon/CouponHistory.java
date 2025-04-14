package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자의 쿠폰 발급 및 사용 이력
 * <p>
 * - 유저가 어떤 쿠폰을 발급받았는지 관리
 * - 쿠폰의 사용 가능 여부 확인
 * - 쿠폰 사용 처리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CouponHistory {

    @Id
    private Long id;
    private Long userId;
    private Long couponId;
    private LocalDateTime issueDate;
    private LocalDateTime useDate;
    @Enumerated(EnumType.STRING)
    private CouponUsageStatus couponUsageStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CouponHistory(Long id,
                         Long userId,
                         Long couponId,
                         LocalDateTime issueDate,
                         LocalDateTime useDate,
                         CouponUsageStatus couponUsageStatus,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {

        if (couponId == null) {
            throw new CustomException(ErrorType.INVALID_COUPON_ISSUE_DATA);
        }

        if (couponUsageStatus == null) {
            throw new CustomException(ErrorType.INVALID_COUPON_STATUS);
        }

        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.issueDate = issueDate;
        this.useDate = useDate;
        this.couponUsageStatus = couponUsageStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 쿠폰 발급 이력 생성
     */
    public static CouponHistory issue(Long userId, Coupon coupon) {
        return new CouponHistory(
                null,
                userId,
                coupon.getId(),
                LocalDateTime.now(),
                null,
                CouponUsageStatus.AVAILABLE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * 쿠폰이 사용 가능한 상태인지 확인
     */
    public boolean isAvailable() {
        return this.couponUsageStatus == CouponUsageStatus.AVAILABLE &&
                this.useDate == null;
    }

    /**
     * 쿠폰 사용 처리 (중복 사용 방지)
     */
    public void use() {
        if (!isAvailable()) {
            throw new CustomException(ErrorType.COUPON_ALREADY_USED);
        }
        this.couponUsageStatus = CouponUsageStatus.USED;
        this.useDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 소유자가 맞는지 검증
     */
    public void validateOwner(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new CustomException(ErrorType.UNAUTHORIZED_COUPON_ACCESS);
        }
    }
}
