package com.hhplusecommerce.api.balance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "잔액 응답")
public record BalanceResponse(
        @Schema(description = "현재 잔액", example = "10000")
        Long amount
) {}
