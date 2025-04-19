package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_history")
@EntityListeners(AuditingEntityListener.class)
public class CouponHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime issueDate;

    private LocalDateTime useDate;

    @Enumerated(EnumType.STRING)
    private CouponUsageStatus couponUsageStatus;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "coupon_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Coupon coupon;

    public CouponHistory(Long userId,
                         LocalDateTime issueDate,
                         LocalDateTime useDate,
                         CouponUsageStatus couponUsageStatus,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt,
                         Coupon coupon) {

        if (coupon == null) {
            throw new CustomException(ErrorType.INVALID_COUPON_ISSUE_DATA);
        }

        if (couponUsageStatus == null) {
            throw new CustomException(ErrorType.INVALID_COUPON_STATUS);
        }

        this.userId = userId;
        this.issueDate = issueDate;
        this.useDate = useDate;
        this.couponUsageStatus = couponUsageStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.coupon = coupon;
    }

    /**
     * 쿠폰 발급 이력 생성
     */
    public static CouponHistory issue(Long userId, Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        return new CouponHistory(
                userId,
                now,
                null,
                CouponUsageStatus.AVAILABLE,
                now,
                now,
                coupon
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
