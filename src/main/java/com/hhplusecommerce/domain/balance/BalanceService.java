package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.hhplusecommerce.domain.balance.BalanceChangeType.CHARGE;
import static com.hhplusecommerce.domain.balance.BalanceChangeType.DEDUCT;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    @Transactional(readOnly = true)
    public BalanceResult getBalance(Long userId) {
        UserBalance userBalance = balanceRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        return new BalanceResult(userBalance.getUserId(), userBalance.getAmount());
    }

    @Transactional
    public BalanceResult charge(Long userId, BalanceCommand balanceCommand) {
        UserBalance userBalance = balanceRepository.findByUserId(userId).orElseGet(() -> UserBalance.initialize(userId));

        BigDecimal beforeBalance = userBalance.getAmount();
        userBalance.charge(balanceCommand.amount());

        BalanceHistory balanceHistory = BalanceHistory.createBalanceHistory(userBalance.getUserId(), beforeBalance, userBalance.getAmount(), CHARGE);
        balanceHistoryRepository.save(balanceHistory);

        return new BalanceResult(userBalance.getUserId(), userBalance.getAmount());
    }

    @Transactional
    public BalanceResult deduct(Long userId, BalanceCommand balanceCommand) {
        UserBalance userBalance = balanceRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        BigDecimal beforeBalance = userBalance.getAmount();
        userBalance.deduct(balanceCommand.amount());

        BalanceHistory balanceHistory = BalanceHistory.createBalanceHistory(userBalance.getUserId(), beforeBalance, userBalance.getAmount(), DEDUCT);
        balanceHistoryRepository.save(balanceHistory);

        return new BalanceResult(userBalance.getUserId(), userBalance.getAmount());
    }
}
