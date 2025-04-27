package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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
        UserBalance userBalance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        return new BalanceResult(userBalance.getUserId(), userBalance.getAmount());
    }

    @Retryable(
            value = { OptimisticLockException.class, ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public BalanceResult chargeBalance(BalanceCommand balanceCommand) {
        final Long userId = balanceCommand.userId();

        UserBalance userBalance = balanceRepository.findOrCreateById(userId);

        BigDecimal before = userBalance.getAmount();
        userBalance.charge(balanceCommand.amount());

        balanceHistoryRepository.save(BalanceHistory.create(
                userId, before, userBalance.getAmount(), CHARGE
        ));

        return new BalanceResult(userId, userBalance.getAmount());
    }

    @Retryable(
            retryFor = { OptimisticLockException.class, ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public BalanceResult deductBalance(BalanceCommand balanceCommand) {
        Long userId = balanceCommand.userId();

        UserBalance userBalance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        BigDecimal previousBalance = userBalance.getAmount();
        userBalance.deduct(balanceCommand.amount());

        balanceHistoryRepository.save(BalanceHistory.create(
                userId, previousBalance, userBalance.getAmount(), DEDUCT
        ));

        return new BalanceResult(userId, userBalance.getAmount());
    }

    @Transactional(readOnly = true)
    public void validateEnough(Long userId, BigDecimal requiredAmount) {
        UserBalance userBalance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_BALANCE_NOT_FOUND));

        userBalance.validateEnoughAmount(requiredAmount);
    }
}
