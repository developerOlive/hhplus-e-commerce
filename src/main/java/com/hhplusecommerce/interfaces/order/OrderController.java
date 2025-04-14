package com.hhplusecommerce.interfaces.order;

import com.hhplusecommerce.application.order.OrderFacade;
import com.hhplusecommerce.application.order.OrderResult;
import com.hhplusecommerce.domain.order.OrderCommand;
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

@RestController
@Tag(name = "주문 API", description = "주문 관련 API")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping("/api/v1/orders")
    @Operation(summary = "주문서 작성", description = "결제 전 주문서를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 성공", content = @Content(examples = @ExampleObject(value = OrderSwaggerDocs.ORDER_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "요청 오류", content = @Content(
                    examples = {
                            @ExampleObject(name = "잔액 부족", value = OrderSwaggerDocs.INSUFFICIENT_BALANCE),
                            @ExampleObject(name = "재고 부족", value = OrderSwaggerDocs.INSUFFICIENT_STOCK),
                            @ExampleObject(name = "유효하지 않은 쿠폰", value = OrderSwaggerDocs.COUPON_INACTIVE)}))
    })
    public ResponseEntity<ApiResult<OrderResponse>> placeOrder(@RequestBody @Valid OrderRequest request) {
        OrderCommand orderCommand = request.toCommand();
        OrderResult orderResult = orderFacade.placeOrder(orderCommand);

        return ResponseEntity.ok(ApiResult.success(OrderResponse.from(orderResult)));
    }
}
