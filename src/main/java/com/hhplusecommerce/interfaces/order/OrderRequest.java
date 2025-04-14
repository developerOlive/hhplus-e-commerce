package com.hhplusecommerce.interfaces.order;

import com.hhplusecommerce.domain.order.OrderCommand;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.interfaces.order.OrderItemRequest;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "주문서 작성 요청")
public class OrderRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다.")
    @ArraySchema(schema = @Schema(implementation = OrderItemRequest.class), minItems = 1)
    private List<OrderItemRequest> orderItems;

    @Schema(description = "쿠폰 발급 ID (선택)", example = "5001")
    private Long couponIssueId;

    public OrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = orderItems.stream()
                .map(OrderItemRequest::toCommand)
                .toList();

        return new OrderCommand(
                userId,
                couponIssueId,
                itemCommands
        );
    }
}
