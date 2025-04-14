package com.hhplusecommerce.interfaces.balance;

import com.hhplusecommerce.domain.balance.BalanceResult;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.support.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.hhplusecommerce.interfaces.balance.BalanceSwaggerDocs.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "잔액 API", description = "사용자 잔액 관련 API")
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/api/v1/users/{userId}/balance")
    @Operation(summary = "잔액 조회", description = "사용자의 현재 잔액을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(examples = @ExampleObject(value = BALANCE_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "사용자 잔액정보 없음", content = @Content(examples = @ExampleObject(value = USER_BALANCE_NOT_FOUND)))
    })
    public ResponseEntity<ApiResult<BalanceResponse>> getBalance(@PathVariable Long userId) {
        BalanceResult balanceResult = balanceService.getBalance(userId);
        return ResponseEntity.ok(ApiResult.success(BalanceResponse.from(balanceResult)));
    }

    @PostMapping("/api/v1/users/{userId}/balance/charge")
    @Operation(summary = "잔액 충전", description = "사용자의 잔액을 충전합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "충전 성공", content = @Content(examples = @ExampleObject(value = BALANCE_CHARGE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "음수 금액인 경우", content = @Content(examples = @ExampleObject(value = INVALID_BALANCE_AMOUNT))),
            @ApiResponse(responseCode = "404", description = "사용자 잔액정보 없음", content = @Content(examples = @ExampleObject(value = USER_BALANCE_NOT_FOUND)))
    })
    public ResponseEntity<ApiResult<BalanceResponse>> chargeBalance(@PathVariable Long userId,
                                                                    @Valid @RequestBody BalanceRequest request) {
        BalanceResult balanceResult = balanceService.chargeBalance(userId, request.toCommand());
        return ResponseEntity.ok(ApiResult.success(BalanceResponse.from(balanceResult)));
    }

    @PostMapping("/api/v1/users/{userId}/balance/deduct")
    @Operation(summary = "잔액 차감", description = "사용자의 잔액을 차감합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차감 성공", content = @Content(examples = @ExampleObject(value = BALANCE_DEDUCT_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "요청 오류", content = @Content(examples = {
                    @ExampleObject(name = "잔액 부족", value = INSUFFICIENT_BALANCE),
                    @ExampleObject(name = "음수 금액인 경우", value = INVALID_BALANCE_AMOUNT)
            })),
            @ApiResponse(responseCode = "404", description = "사용자 잔액정보 없음", content = @Content(examples = @ExampleObject(value = USER_BALANCE_NOT_FOUND)))
    })
    public ResponseEntity<ApiResult<BalanceResponse>> deductBalance(@PathVariable Long userId,
                                                                    @Valid @RequestBody BalanceRequest request) {
        BalanceResult balanceResult = balanceService.deductBalance(userId, request.toCommand());
        return ResponseEntity.ok(ApiResult.success(BalanceResponse.from(balanceResult)));
    }
}
