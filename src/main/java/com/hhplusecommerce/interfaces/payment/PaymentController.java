package com.hhplusecommerce.interfaces.payment;

import com.hhplusecommerce.application.payment.PaymentFacade;
import com.hhplusecommerce.application.payment.PaymentResult;
import com.hhplusecommerce.interfaces.order.OrderSwaggerDocs;
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
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "결제 API", description = "결제 관련 API")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/api/v1/payments")
    @Operation(summary = "결제 요청", description = "주문에 대한 결제를 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 성공", content = @Content(examples = @ExampleObject(value = PaymentSwaggerDocs.PAYMENT_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "결제 오류", content = @Content(
                    examples = {
                            @ExampleObject(name = "주문 데이터 확인 불가", value = PaymentSwaggerDocs.ORDER_NOT_FOUND),
                            @ExampleObject(name = "잔액 부족", value = PaymentSwaggerDocs.INSUFFICIENT_BALANCE),
                            @ExampleObject(name = "이미 사용된 쿠폰", value = PaymentSwaggerDocs.COUPON_ALREADY_USED),
                    }))
    })
    public ResponseEntity<ApiResult<PaymentResponse>> requestPayment(@RequestBody @Valid PaymentRequest request) {
        PaymentResult result = paymentFacade.completePayment(request.toCommand());

        return ResponseEntity.ok(ApiResult.success(PaymentResponse.from(result)));
    }
}
