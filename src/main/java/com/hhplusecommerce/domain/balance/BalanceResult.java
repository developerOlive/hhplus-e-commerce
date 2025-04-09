package com.hhplusecommerce.domain.balance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResult {
    private long userId;
    private BigDecimal balance;
}
