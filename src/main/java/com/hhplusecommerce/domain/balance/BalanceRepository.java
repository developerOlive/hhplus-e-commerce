package com.hhplusecommerce.domain.balance;

import java.util.Optional;

public interface BalanceRepository {
    Optional<UserBalance> findByUserId(Long userId);

    UserBalance save(UserBalance userBalance);

    default UserBalance findOrCreateById(Long userId) {
        return findByUserId(userId)
                .orElseGet(() -> save(UserBalance.initialize(userId)));
    }
}
