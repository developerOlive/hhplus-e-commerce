package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class UserBalance {

    @Id
    private Long userId;

    private BigDecimal amount;

    private LocalDateTime updatedAt;

    @Builder
    public UserBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }
        this.userId = userId;
        this.amount = amount;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserBalance initialize(Long userId) {
        return new UserBalance(userId, BigDecimal.ZERO);
    }

    /**
     * 잔액 충전
     */
    public void charge(BigDecimal chargeAmount) {
        validatePositiveAmount(chargeAmount);
        this.amount = this.amount.add(chargeAmount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 잔액 차감
     */
    public void deduct(BigDecimal deductAmount) {
        validatePositiveAmount(deductAmount);
        if (this.amount.compareTo(deductAmount) < 0) {
            throw new CustomException(ErrorType.INSUFFICIENT_BALANCE);
        }
        this.amount = this.amount.subtract(deductAmount);
        this.updatedAt = LocalDateTime.now();
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
        }
    }
}
