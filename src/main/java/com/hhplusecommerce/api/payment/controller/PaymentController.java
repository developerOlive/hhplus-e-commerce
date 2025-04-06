package com.hhplusecommerce.api.payment.controller;

import com.hhplusecommerce.api.payment.dto.PaymentRequest;
import com.hhplusecommerce.api.payment.dto.PaymentResponse;
import com.hhplusecommerce.common.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.hhplusecommerce.api.payment.docs.PaymentSwaggerDocs.*;

@RestController
@Tag(name = "결제 API", description = "결제 관련 API")
public class PaymentController {

    @PostMapping("/api/v1/payments")
    @Operation(summary = "결제 요청", description = "주문에 대한 결제를 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 성공", content = @Content(examples = @ExampleObject(value = PAYMENT_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "결제 실패", content = @Content(examples = @ExampleObject(value = PAYMENT_FAILED)))
    })
    public ResponseEntity<ApiResult<PaymentResponse>> requestPayment(@RequestBody @Valid PaymentRequest request) {
        PaymentResponse response = PaymentResponse.of(5001L, 20000L, "SUCCESS");
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
