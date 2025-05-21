package com.hhplusecommerce.interfaces.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.model.CouponResult;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.interfaces.coupon.CouponResponse.CouponDetail;
import com.hhplusecommerce.interfaces.coupon.CouponResponse.Issue;
import com.hhplusecommerce.support.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hhplusecommerce.interfaces.coupon.CouponSwaggerDocs.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "쿠폰 API", description = "사용자 쿠폰 관련 API")
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/api/v1/users/{userId}/coupons")
    @Operation(summary = "보유 쿠폰 목록 조회", description = "사용자가 보유한 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = COUPON_LIST_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 쿠폰 없음", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_FAIL_NOT_FOUND)))
    })
    public ResponseEntity<ApiResult<List<CouponDetail>>> getUserCoupons(@PathVariable("userId") Long userId) {
        List<CouponResult> results = couponService.getUserCoupons(userId);
        List<CouponDetail> responses = results.stream()
                .map(CouponDetail::from)
                .toList();

        return ResponseEntity.ok(ApiResult.success(responses));
    }

    @PostMapping("/api/v1/users/{userId}/coupons/{couponId}/issue")
    @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "쿠폰 소진", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_FAIL_NO_STOCK))),
            @ApiResponse(responseCode = "409", description = "이미 발급된 쿠폰", content = @Content(examples = @ExampleObject(value = COUPON_ISSUE_FAIL_ALREADY)))
    })
    public ResponseEntity<ApiResult<Issue>> issueCoupon(
            @PathVariable("userId") Long userId,
            @PathVariable("couponId") Long couponId
    ) {
        CouponCommand command = new CouponCommand(userId, couponId);
        Long couponHistoryId = couponService.issueCoupon(command);

        return ResponseEntity.ok(ApiResult.success(Issue.from(couponHistoryId)));
    }
}
