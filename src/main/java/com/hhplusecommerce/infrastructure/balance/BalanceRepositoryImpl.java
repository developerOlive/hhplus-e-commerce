package com.hhplusecommerce.infrastructure.balance;

import com.hhplusecommerce.domain.balance.BalanceRepository;
import com.hhplusecommerce.domain.balance.UserBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BalanceRepositoryImpl implements BalanceRepository {

    private final BalanceJpaRepository balanceJpaRepository;

    @Override
    public Optional<UserBalance> findByUserId(Long userId) {
        return balanceJpaRepository.findByUserId(userId);
    }

    @Override
    public UserBalance save(UserBalance userBalance) {
        return balanceJpaRepository.save(userBalance);
    }

}
