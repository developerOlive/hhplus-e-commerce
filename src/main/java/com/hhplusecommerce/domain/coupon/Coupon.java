package com.hhplusecommerce.domain.coupon;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Coupon {

    @Id
    private Long id;
    private String couponName;
    @Enumerated(EnumType.STRING)
    private CouponDiscountType discountType;
    private BigDecimal discountValue;
    private int maxQuantity;
    private int issuedQuantity = 0;
    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus = CouponStatus.ACTIVE;
    private LocalDate validStartDate;
    private LocalDate validEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Coupon(String couponName, CouponDiscountType discountType, BigDecimal discountValue, int maxQuantity, LocalDate validStartDate, LocalDate validEndDate) {
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_COUPON_VALUE);
        }
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxQuantity = maxQuantity;
        this.validStartDate = validStartDate;
        this.validEndDate = validEndDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰이 사용 가능한지 체크
     * 유효기간이 지난 쿠폰은 사용 불가
     */
    public boolean isAvailable() {
        return couponStatus == CouponStatus.ACTIVE &&
                LocalDate.now().isAfter(validStartDate) &&
                LocalDate.now().isBefore(validEndDate);
    }

    /**
     * 쿠폰 발급 수량 증가
     */
    public void increaseIssuedQuantity() {
        if (this.issuedQuantity >= this.maxQuantity) {
            throw new CustomException(ErrorType.COUPON_ISSUE_LIMIT_EXCEEDED);
        }
        this.issuedQuantity++;
        this.updatedAt = LocalDateTime.now();
    }
}
