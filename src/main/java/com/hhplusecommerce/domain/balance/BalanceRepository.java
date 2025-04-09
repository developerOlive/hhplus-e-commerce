package com.hhplusecommerce.domain.balance;

import java.util.Optional;

public interface BalanceRepository {
    Optional<UserBalance> findByUserId(Long userId);
}
