package com.hhplusecommerce.interfaces.balance;

import com.hhplusecommerce.domain.balance.BalanceResult;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BalanceResponse {

    private long userId;
    private BigDecimal balance;

    public static BalanceResponse from(BalanceResult balanceResult) {
        return new BalanceResponse(balanceResult.getUserId(), balanceResult.getBalance());
    }
}
