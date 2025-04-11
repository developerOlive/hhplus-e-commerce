package com.hhplusecommerce.interfaces.balance;

import com.hhplusecommerce.domain.balance.BalanceCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BalanceRequest {

    @Schema(description = "충전 또는 차감할 금액", example = "10000")
    @NotNull
    @DecimalMin(value = "1", inclusive = true)
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public BalanceCommand toCommand() {
        return new BalanceCommand(amount);
    }
}
