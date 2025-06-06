package com.hhplusecommerce.interfaces.coupon;

import com.hhplusecommerce.domain.coupon.type.CouponDiscountType;
import com.hhplusecommerce.domain.coupon.model.CouponResult;
import com.hhplusecommerce.domain.coupon.type.CouponStatus;
import com.hhplusecommerce.domain.coupon.type.CouponUsageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CouponResponse {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponDetail {

        @Schema(description = "쿠폰 ID", example = "1")
        private Long couponId;

        @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
        private String couponName;

        @Schema(description = "할인 유형", example = "FIXED_RATE")
        private CouponDiscountType discountType;

        @Schema(description = "할인 값", example = "10")
        private BigDecimal discountValue;

        @Schema(description = "쿠폰 상태", example = "ACTIVE")
        private CouponStatus couponStatus;

        @Schema(description = "쿠폰 사용 상태", example = "AVAILABLE")
        private CouponUsageStatus couponUsageStatus;

        @Schema(description = "유효 시작일", example = "2025-04-01")
        private LocalDate validStartDate;

        @Schema(description = "유효 종료일", example = "2025-04-30")
        private LocalDate validEndDate;

        public static CouponDetail from(CouponResult result) {
            return new CouponDetail(
                    result.couponId(),
                    result.couponName(),
                    result.discountType(),
                    result.discountValue(),
                    result.couponStatus(),
                    result.couponUsageStatus(),
                    result.validStartDate(),
                    result.validEndDate()
            );
        }
    }

    @Schema(description = "쿠폰 발급 응답")
    public record Issue(
            @Schema(description = "쿠폰 발급 이력 ID", example = "1001")
            Long couponHistoryId
    ) {
        public static Issue from(Long couponHistoryId) {
            return new Issue(couponHistoryId);
        }
    }
}
