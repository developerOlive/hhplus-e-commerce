package com.hhplusecommerce.interfaces.order.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "주문 요청")
public class OrderRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @ArraySchema(schema = @Schema(implementation = OrderItemRequest.class), minItems = 1)
    private List<OrderItemRequest> orderItems;

    @Schema(description = "쿠폰 발급 ID (선택)", example = "5001")
    private Long couponIssueId;
}
