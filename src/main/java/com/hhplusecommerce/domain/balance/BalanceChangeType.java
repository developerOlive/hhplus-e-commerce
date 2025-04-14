package com.hhplusecommerce.domain.balance;

public enum BalanceChangeType {
    CHARGE("충전"),
    DEDUCT("차감");

    private final String description;

    BalanceChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String value) {
        for (BalanceChangeType type : BalanceChangeType.values()) {
            if (type.name().equals(value)) {
                return true;
            }
        }

        return false;
    }
}
