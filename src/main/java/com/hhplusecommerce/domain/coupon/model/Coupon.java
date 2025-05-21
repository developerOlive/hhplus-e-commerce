package com.hhplusecommerce.domain.coupon.model;

import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.domain.coupon.type.CouponDiscountType;
import com.hhplusecommerce.domain.coupon.type.CouponStatus;
import com.hhplusecommerce.domain.coupon.type.CouponType;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 쿠폰 정책
 * <p>
 * - 쿠폰의 할인 조건 및 정책 관리
 * - 유효한 쿠폰인지 판단 (기간, 상태 등)
 * - 발급 수량 관리
 * - 주문 금액에 대한 할인 금액 계산
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Enumerated(EnumType.STRING)
    private CouponType couponType;
    @Enumerated(EnumType.STRING)
    private CouponIssueStatus issueStatus = CouponIssueStatus.READY;
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Coupon(String couponName,
                  CouponDiscountType discountType,
                  BigDecimal discountValue,
                  int maxQuantity,
                  LocalDate validStartDate,
                  LocalDate validEndDate,
                  int issuedQuantity,
                  CouponType couponType) {

        if (couponName == null || couponName.isBlank()) {
            throw new CustomException(ErrorType.INVALID_COUPON_NAME);
        }

        if (discountType == null) {
            throw new CustomException(ErrorType.INVALID_COUPON_DISCOUNT_TYPE);
        }

        validateDiscountValue(discountValue);

        if (maxQuantity < 1) {
            throw new CustomException(ErrorType.INVALID_COUPON_QUANTITY);
        }

        if (validStartDate == null || validEndDate == null || !validStartDate.isBefore(validEndDate)) {
            throw new CustomException(ErrorType.INVALID_COUPON_DATE_RANGE);
        }

        if (couponType == null) {
            throw new CustomException(ErrorType.INVALID_COUPON_TYPE);
        }

        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxQuantity = maxQuantity;
        this.issuedQuantity = issuedQuantity;
        this.validStartDate = validStartDate;
        this.validEndDate = validEndDate;
        this.couponType = couponType;
        this.couponStatus = CouponStatus.ACTIVE;
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
     * 쿠폰을 적용한 할인 금액 계산
     */
    public BigDecimal discountFor(BigDecimal totalAmount) {
        validateDiscountValue(this.discountValue);

        return discountType.discount(totalAmount, discountValue);
    }

    /**
     * 할인 금액 유효성 검증
     */
    private void validateDiscountValue(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_COUPON_VALUE);
        }
    }

    /**
     * 쿠폰 발급 상태 완료 처리 (재고 소진)
     */
    public void completeIssue() {
        this.issueStatus = CouponIssueStatus.FINISHED;
    }
}
