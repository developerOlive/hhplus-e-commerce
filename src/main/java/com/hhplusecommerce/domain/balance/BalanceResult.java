package com.hhplusecommerce.domain.balance;

import java.math.BigDecimal;

public record BalanceResult(Long userId, BigDecimal balance) {}
