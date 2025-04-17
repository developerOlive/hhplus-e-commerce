package com.hhplusecommerce.infrastructure.balance;

import com.hhplusecommerce.domain.balance.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistory, Long> {
    List<BalanceHistory> findByUserBalance_UserId(Long userId);
}
