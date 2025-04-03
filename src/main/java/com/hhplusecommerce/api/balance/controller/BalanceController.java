package com.hhplusecommerce.api.balance.controller;

import com.hhplusecommerce.api.balance.dto.BalanceChargeRequest;
import com.hhplusecommerce.api.balance.dto.BalanceResponse;
import com.hhplusecommerce.common.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.hhplusecommerce.api.balance.docs.BalanceSwaggerDocs.*;

@RestController
@Tag(name = "잔액 API", description = "사용자 잔액 관련 API")
public class BalanceController {

    @GetMapping("/api/v1/users/{userId}/balance")
    @Operation(summary = "잔액 조회", description = "사용자의 현재 잔액을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(examples = @ExampleObject(value = BALANCE_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(examples = @ExampleObject(value = BALANCE_NOT_FOUND)))
    })
    public ResponseEntity<ApiResult<BalanceResponse>> getBalance(@PathVariable Long userId) {
        if (userId <= 0) {
            return ResponseEntity.status(404).body(ApiResult.fail("존재하지 않는 사용자입니다."));
        }

        return ResponseEntity.ok(ApiResult.success(new BalanceResponse(10_000L)));
    }

    @PostMapping("/api/v1/users/{userId}/balance/charge")
    @Operation(summary = "잔액 충전", description = "사용자의 잔액을 충전합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "충전 성공", content = @Content(examples = @ExampleObject(value = BALANCE_CHARGE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "금액 누락", content = @Content(examples = @ExampleObject(value = BALANCE_CHARGE_AMOUNT_REQUIRED))),
            @ApiResponse(responseCode = "400", description = "금액이 0 이하", content = @Content(examples = @ExampleObject(value = BALANCE_CHARGE_AMOUNT_POSITIVE))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(examples = @ExampleObject(value = BALANCE_CHARGE_USER_NOT_FOUND))),
            @ApiResponse(responseCode = "500", description = "충전 실패", content = @Content(examples = @ExampleObject(value = BALANCE_CHARGE_FAILED)))
    })
    public ResponseEntity<ApiResult<BalanceResponse>> chargeBalance(
            @PathVariable Long userId,
            @RequestBody BalanceChargeRequest request
    ) {
        Long updatedAmount = 10_000L + request.getAmount();

        return ResponseEntity.ok(ApiResult.success(new BalanceResponse(updatedAmount)));
    }
}
