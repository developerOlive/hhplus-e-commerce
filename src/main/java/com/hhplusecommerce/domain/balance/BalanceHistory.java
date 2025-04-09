package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private BigDecimal amount;
    private BigDecimal beforeBalance;
    private BigDecimal afterBalance;
    @Enumerated(EnumType.STRING)
    private BalanceChangeType changeType;
    private LocalDateTime createdAt;

    public static BalanceHistory createBalanceHistory(Long userId, BigDecimal beforeBalance, BigDecimal afterBalance, BalanceChangeType changeType) {
        BalanceHistory balanceHistory = new BalanceHistory();
        balanceHistory.validateChangeType(changeType);
        BigDecimal amount = balanceHistory.calculateAmount(beforeBalance, afterBalance);

        return new BalanceHistory(
                null,
                userId,
                amount,
                beforeBalance,
                afterBalance,
                changeType,
                LocalDateTime.now()
        );
    }

    private void validateChangeType(BalanceChangeType changeType) {
        if (changeType == null || !BalanceChangeType.isValid(changeType.name())) {
            throw new CustomException(ErrorType.INVALID_BALANCE_CHANGE_TYPE);
        }
    }

    private BigDecimal calculateAmount(BigDecimal beforeBalance, BigDecimal afterBalance) {
        BigDecimal amount = afterBalance.subtract(beforeBalance);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }

        return amount;
    }
}
