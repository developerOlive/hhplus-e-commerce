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
    public BalanceResult chargeBalance(Long userId, BalanceCommand balanceCommand) {
        UserBalance userBalance = balanceRepository.findByUserId(userId).orElseGet(() -> UserBalance.initialize(userId));

        BigDecimal previousBalance = userBalance.getAmount();
        userBalance.charge(balanceCommand.amount());

        BalanceHistory balanceHistory = BalanceHistory.create(userBalance.getUserId(), previousBalance, userBalance.getAmount(), CHARGE);
        balanceHistoryRepository.save(balanceHistory);

        return new BalanceResult(userBalance.getUserId(), userBalance.getAmount());
    }

    @Transactional
    public BalanceResult deductBalance(Long userId, BalanceCommand balanceCommand) {
        UserBalance userBalance = balanceRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        BigDecimal previousBalance = userBalance.getAmount();
        userBalance.deduct(balanceCommand.amount());

        BalanceHistory balanceHistory = BalanceHistory.create(userBalance.getUserId(), previousBalance, userBalance.getAmount(), DEDUCT);
        balanceHistoryRepository.save(balanceHistory);

        return new BalanceResult(userBalance.getUserId(), userBalance.getAmount());
    }

    @Transactional(readOnly = true)
    public void validateEnough(Long userId, BigDecimal requiredAmount) {
        UserBalance userBalance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        userBalance.validateEnoughAmount(requiredAmount);
    }
}
