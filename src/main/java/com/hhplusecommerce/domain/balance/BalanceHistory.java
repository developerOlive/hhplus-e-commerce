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

    public static BalanceHistory create(Long userId, BigDecimal beforeBalance, BigDecimal afterBalance, BalanceChangeType changeType) {
        if (userId == null) {
            throw new CustomException(ErrorType.USER_BALANCE_NOT_FOUND);
        }
        if (beforeBalance == null || afterBalance == null) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }
        if (beforeBalance.compareTo(BigDecimal.ZERO) < 0 || afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }
        if (changeType == null || !BalanceChangeType.isValid(changeType.name())) {
            throw new CustomException(ErrorType.INVALID_BALANCE_CHANGE_TYPE);
        }

        BigDecimal amount = afterBalance.subtract(beforeBalance);
        changeType.validate(amount);

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
}
