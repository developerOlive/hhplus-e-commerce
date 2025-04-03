package com.hhplusecommerce.api.coupon.controller;

import com.hhplusecommerce.api.coupon.dto.CouponIssueResponse;
import com.hhplusecommerce.api.coupon.dto.CouponResponse;
import com.hhplusecommerce.common.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.hhplusecommerce.api.coupon.docs.CouponSwaggerDocs.*;

@RestController
@Tag(name = "쿠폰 API", description = "사용자 쿠폰 관련 API")
public class CouponController {

    @GetMapping("/api/v1/users/{userId}/coupons")
    @Operation(summary = "보유 쿠폰 목록 조회", description = "사용자가 보유한 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = COUPON_LIST_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(examples = @ExampleObject(value = COUPON_LIST_NOT_FOUND)))
    })
    public ResponseEntity<ApiResult<List<CouponResponse>>> getUserCoupons( @PathVariable("userId") Long userId) {
        List<CouponResponse> coupons = List.of(
                new CouponResponse(1L, "10% 할인 쿠폰", "FIXED_RATE", 10, "AVAILABLE", LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30)),
                new CouponResponse(2L, "1,000원 할인 쿠폰", "FIXED_AMOUNT", 1000, "AVAILABLE", LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30))
        );

        return ResponseEntity.ok(ApiResult.success(coupons));
    }

    @PostMapping("/api/v1/users/{userId}/coupons/{couponId}/issue")
    @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "쿠폰 소진", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_FAIL_NO_STOCK))),
            @ApiResponse(responseCode = "409", description = "이미 발급됨", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_FAIL_ALREADY)))
    })
    public ResponseEntity<ApiResult<CouponIssueResponse>> issueCoupon(
            @PathVariable("userId") Long userId,
            @PathVariable("couponId") Long couponId
    ) {

        return ResponseEntity.ok(ApiResult.success(new CouponIssueResponse(100L)));
    }
}
