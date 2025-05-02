package com.hhplusecommerce.concurrency.balance;

import com.hhplusecommerce.concurrency.ConcurrencyResult;
import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceRepository;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.balance.UserBalance;
import com.hhplusecommerce.support.ConcurrencyTestSupport;
import com.hhplusecommerce.support.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class BalanceConcurrencyTest extends ConcurrencyTestSupport {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private BalanceRepository balanceRepository;

    private static final Long USER_ID = 1L;
    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10000);
    private static final BigDecimal CHARGE_AMOUNT = BigDecimal.valueOf(1000);
    private static final int CONCURRENCY_THREADS = 2;
    private static final BigDecimal EXPECTED_AMOUNT = INITIAL_BALANCE.add(CHARGE_AMOUNT);

    @BeforeEach
    void setUp() {
        balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));
    }

    @Nested
    class 잔액_충전_동시성 {

        @Test
        void 동시에_충전_요청_시_하나만_성공한다() throws InterruptedException {
            ConcurrencyResult result = executeWithLatch(CONCURRENCY_THREADS, r -> {
                long userId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
                log.info("🟡 [{}] 잔액 충전 시도", userId);
                try {
                    balanceService.chargeBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));
                    r.success();
                } catch (Exception e) {
                    log.error("🔴 [{}] 충전 실패: {}", userId, e.getMessage());
                    r.error();
                }
            });

            UserBalance userBalance = balanceRepository.findByUserId(USER_ID).orElseThrow();
            BigDecimal finalAmount = userBalance.getAmount();

            log.warn("▶ 최종 잔액: {}", finalAmount.stripTrailingZeros().toPlainString());
            log.warn("▶ 성공한 충전 횟수: {}", result.getSuccessCount());
            log.warn("▶ 실패한 충전 횟수: {}", result.getErrorCount());

            Assertions.assertEquals(1, result.getSuccessCount());
            Assertions.assertEquals(EXPECTED_AMOUNT.stripTrailingZeros().toPlainString(), finalAmount.stripTrailingZeros().toPlainString());
        }
    }

    @Nested
    class 잔액_차감_동시성 {

        private static final BigDecimal INITIAL_AMOUNT = BigDecimal.valueOf(5000);
        private static final BigDecimal DEDUCT_AMOUNT = BigDecimal.valueOf(3000);

        @BeforeEach
        void setUpDeduct() {
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_AMOUNT));
        }

        @Test
        void 동시에_차감_요청_시_하나만_성공한다() throws InterruptedException {
            ConcurrencyResult result = executeWithLatch(CONCURRENCY_THREADS, r -> {
                long userId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
                log.info("🟡 [{}] 잔액 차감 시도", userId);
                try {
                    balanceService.deductBalance(new BalanceCommand(USER_ID, DEDUCT_AMOUNT));
                    r.success();
                } catch (Exception e) {
                    log.error("🔴 [{}] 차감 실패: {}", userId, e.getMessage());
                    r.error();
                }
            });

            UserBalance userBalance = balanceRepository.findByUserId(USER_ID).orElseThrow();
            BigDecimal finalAmount = userBalance.getAmount();
            BigDecimal expectedAmount = INITIAL_AMOUNT.subtract(DEDUCT_AMOUNT);

            log.warn("▶ 최종 잔액: {}", finalAmount.stripTrailingZeros().toPlainString());
            log.warn("▶ 성공한 차감 횟수: {}", result.getSuccessCount());
            log.warn("▶ 실패한 차감 횟수: {}", result.getErrorCount());

            Assertions.assertEquals(1, result.getSuccessCount());
            Assertions.assertEquals(expectedAmount.stripTrailingZeros().toPlainString(), finalAmount.stripTrailingZeros().toPlainString());
        }
    }

    @Nested
    class 잔액_순차_요청_정합성_테스트 {

        private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(500);
        private static final BigDecimal CHARGE_AMOUNT = BigDecimal.valueOf(1000);
        private static final BigDecimal EXPECTED_BALANCE_AFTER_BOTH = BigDecimal.valueOf(500);
        private static final BigDecimal EXPECTED_BALANCE_AFTER_CHARGE_ONLY = BigDecimal.valueOf(1500);

        @Test
        void 충전_후_차감_순서로_요청하면_정상적으로_잔액이_감소한다() {
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));

            balanceService.chargeBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));
            balanceService.deductBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));

            UserBalance userBalance = balanceRepository.findByUserId(USER_ID).orElseThrow();
            BigDecimal finalAmount = userBalance.getAmount();

            log.warn("최종 잔액: {}", finalAmount.stripTrailingZeros().toPlainString());

            Assertions.assertEquals(EXPECTED_BALANCE_AFTER_BOTH.stripTrailingZeros().toPlainString(), finalAmount.stripTrailingZeros().toPlainString());
        }

        @Test
        void 차감_후_충전_순서로_요청하면_차감은_실패하고_충전만_성공한다() {
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));

            Assertions.assertThrows(CustomException.class, () ->
                    balanceService.deductBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT)));
            balanceService.chargeBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));

            UserBalance userBalance = balanceRepository.findByUserId(USER_ID).orElseThrow();
            BigDecimal finalAmount = userBalance.getAmount();

            log.warn("최종 잔액: {}", finalAmount.stripTrailingZeros().toPlainString());

            Assertions.assertEquals(EXPECTED_BALANCE_AFTER_CHARGE_ONLY.stripTrailingZeros().toPlainString(), finalAmount.stripTrailingZeros().toPlainString());
        }
    }
}
