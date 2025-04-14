package com.hhplusecommerce.domain.user;

public enum UserStatus {

    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
