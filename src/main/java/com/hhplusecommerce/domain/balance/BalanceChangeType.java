package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;

import java.math.BigDecimal;
import java.util.Arrays;

public enum BalanceChangeType {

    CHARGE("충전") {
        @Override
        public void validate(BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
            }
        }
    },

    DEDUCT("차감") {
        @Override
        public void validate(BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                throw new CustomException(ErrorType.INVALID_BALANCE_AMOUNT);
            }
        }
    };

    private final String description;

    BalanceChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract void validate(BigDecimal amount);

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(type -> type.name().equals(value));
    }
}
