package com.hhplusecommerce.domain.balance;

import org.springframework.stereotype.Repository;

@Repository
public interface BalanceHistoryRepository {
    void save(BalanceHistory balanceHistory);
}
