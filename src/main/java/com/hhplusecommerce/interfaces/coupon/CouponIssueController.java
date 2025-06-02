package com.hhplusecommerce.interfaces.coupon;

import com.hhplusecommerce.application.coupon.CouponIssueFacade;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.support.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "쿠폰 API", description = "사용자 쿠폰 관련 API")
public class CouponIssueController {

    private final CouponIssueFacade couponIssueFacade;

    @PostMapping("/api/v2/users/{userId}/coupons/{couponId}/issue")
    @Operation(summary = "쿠폰 발급 요청", description = "Redis로 중복 및 재고를 확인 후 Kafka로 비동기 발급 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 요청 성공"),
            @ApiResponse(responseCode = "409", description = "이미 발급된 쿠폰"),
            @ApiResponse(responseCode = "400", description = "쿠폰 발급 요청 실패")
    })
    public ResponseEntity<ApiResult<String>> issueCoupon(@PathVariable Long userId, @PathVariable Long couponId) {
        CouponCommand command = new CouponCommand(userId, couponId);
        couponIssueFacade.requestCouponIssue(command);

        return ResponseEntity.ok(ApiResult.success("발급 요청 성공"));
    }
}
