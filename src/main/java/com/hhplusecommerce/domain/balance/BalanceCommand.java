package com.hhplusecommerce.domain.balance;

import java.math.BigDecimal;

public record BalanceCommand(Long userId, BigDecimal amount) {
}
