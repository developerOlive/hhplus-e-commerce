package com.hhplusecommerce.interfaces.order.controller;

import com.hhplusecommerce.interfaces.order.dto.OrderRequest;
import com.hhplusecommerce.interfaces.order.dto.OrderResponse;
import com.hhplusecommerce.support.response.ApiResult;
import com.hhplusecommerce.interfaces.order.docs.OrderSwaggerDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "주문 API", description = "주문 관련 API")
public class OrderController {

    @PostMapping("/api/v1/orders")
    @Operation(summary = "주문 요청", description = "상품 주문을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 성공", content = @Content(examples = @ExampleObject(value = OrderSwaggerDocs.ORDER_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "요청 오류", content = @Content(
                    examples = {
                            @ExampleObject(name = "잔액 부족", value = OrderSwaggerDocs.INSUFFICIENT_BALANCE),
                            @ExampleObject(name = "재고 부족", value = OrderSwaggerDocs.OUT_OF_STOCK),
                            @ExampleObject(name = "유효하지 않은 쿠폰", value = OrderSwaggerDocs.INVALID_COUPON)
                    }
            ))
    })
    public ResponseEntity<ApiResult<OrderResponse>> placeOrder(@RequestBody @Valid OrderRequest request) {
        OrderResponse response = OrderResponse.of(1001L, 25000L, 20000L, "PENDING");
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
