package com.hhplusecommerce.domain.balance;

import java.util.List;

public interface BalanceHistoryRepository {
    void save(BalanceHistory history);

    List<BalanceHistory> findByUserId(Long userId);
}
