package com.hhplusecommerce.interfaces.order;

import com.hhplusecommerce.domain.order.OrderItemCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Schema(description = "주문 상품 항목")
public class OrderItemRequest {

    @Schema(description = "상품 ID", example = "101")
    private Long productId;

    @Schema(description = "수량", example = "2")
    private int quantity;

    @Schema(description = "상품 단가", example = "12000")
    private BigDecimal price;

    @Schema(description = "상품 카테고리", example = "electronics")
    private String category;

    public OrderItemCommand toCommand() {
        return new OrderItemCommand(productId, quantity, price, category);
    }
}
