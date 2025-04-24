package com.hhplusecommerce.infrastructure.balance;

import com.hhplusecommerce.domain.balance.BalanceHistory;
import com.hhplusecommerce.domain.balance.BalanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BalanceHistoryRepositoryImpl implements BalanceHistoryRepository {

    private final BalanceHistoryJpaRepository balanceHistoryJpaRepository;

    @Override
    public void save(BalanceHistory balanceHistory) {
        balanceHistoryJpaRepository.save(balanceHistory);
    }

    @Override
    public List<BalanceHistory> findByUserId(Long userId) {
        return balanceHistoryJpaRepository.findByUserId(userId);
    }
}
