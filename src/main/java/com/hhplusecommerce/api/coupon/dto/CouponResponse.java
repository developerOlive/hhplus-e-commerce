package com.hhplusecommerce.api.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "사용자 보유 쿠폰 응답")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CouponResponse {

    @Schema(description = "쿠폰 ID", example = "1")
    private Long couponId;

    @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
    private String couponName;

    @Schema(description = "할인 유형", example = "FIXED_RATE")
    private String discountType;

    @Schema(description = "할인 값", example = "10")
    private int discountValue;

    @Schema(description = "쿠폰 상태", example = "AVAILABLE")
    private String status;

    @Schema(description = "유효 시작일", example = "2025-04-01")
    private LocalDate validStartDate;

    @Schema(description = "유효 종료일", example = "2025-04-30")
    private LocalDate validEndDate;
}
