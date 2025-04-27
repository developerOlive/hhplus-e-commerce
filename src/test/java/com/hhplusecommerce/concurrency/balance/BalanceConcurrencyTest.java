package com.hhplusecommerce.concurrency.balance;

import com.hhplusecommerce.ConcurrencyTestSupport;
import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceRepository;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.balance.UserBalance;
import com.hhplusecommerce.support.exception.CustomException;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final BigDecimal EXPECTED_AMOUNT = INITIAL_BALANCE.add(CHARGE_AMOUNT); // 하나만 성공

    @BeforeEach
    void setUp() {
        balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));
        log.info("테스트 시작 전 사용자 {}의 잔액 초기화: {}", USER_ID, INITIAL_BALANCE.toPlainString());
    }

    @Nested
    class 잔액_충전_동시성 {

        @Test
        void 동시에_충전_요청_시_하나만_성공한다() throws InterruptedException {
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(CONCURRENCY_THREADS);
            AtomicInteger successCount = new AtomicInteger();

            Runnable task = () -> {
                try {
                    startSignal.await();
                    balanceService.chargeBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                    log.warn("낙관적 락 충돌로 충전 실패");
                } catch (Exception e) {
                    log.error("예외 발생", e);
                } finally {
                    doneSignal.countDown();
                }
            };

            ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENCY_THREADS);
            for (int i = 0; i < CONCURRENCY_THREADS; i++) {
                executorService.submit(task);
            }

            startSignal.countDown();
            doneSignal.await();

            Optional<UserBalance> result = balanceRepository.findByUserId(USER_ID);
            Assertions.assertTrue(result.isPresent(), "잔액 정보가 존재해야 합니다.");

            BigDecimal finalBalance = result.get().getAmount();

            log.warn("▶ 최종 잔액: {}", finalBalance.stripTrailingZeros().toPlainString());
            log.warn("▶ 예상 최종 잔액: {}", EXPECTED_AMOUNT.stripTrailingZeros().toPlainString());
            log.warn("▶ 성공한 충전 횟수: {}", successCount.get());

            Assertions.assertEquals(1, successCount.get(), "충전은 한 번만 성공해야 합니다.");
            Assertions.assertEquals(EXPECTED_AMOUNT.stripTrailingZeros().toPlainString(), finalBalance.stripTrailingZeros().toPlainString(), "충전된 금액이 예상과 다릅니다.");
        }
    }

    @Nested
    class 잔액_차감_동시성 {

        private static final BigDecimal INITIAL_AMOUNT = BigDecimal.valueOf(5000);
        private static final BigDecimal DEDUCT_AMOUNT = BigDecimal.valueOf(3000);

        @BeforeEach
        void setUpDeduct() {
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_AMOUNT));
            log.info("차감 테스트 시작 전 사용자 {}의 잔액 초기화: {}", USER_ID, INITIAL_AMOUNT.toPlainString());
        }

        @Test
        void 동시에_차감_요청_시_하나만_성공한다() throws InterruptedException {
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(CONCURRENCY_THREADS);
            AtomicInteger successCount = new AtomicInteger();

            Runnable task = () -> {
                try {
                    startSignal.await();
                    balanceService.deductBalance(new BalanceCommand(USER_ID, DEDUCT_AMOUNT));
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                    log.warn("낙관적 락 충돌로 차감 실패");
                } catch (Exception e) {
                    log.error("예외 발생", e);
                } finally {
                    doneSignal.countDown();
                }
            };

            ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENCY_THREADS);
            for (int i = 0; i < CONCURRENCY_THREADS; i++) {
                executorService.submit(task);
            }

            startSignal.countDown();
            doneSignal.await();

            Optional<UserBalance> result = balanceRepository.findByUserId(USER_ID);
            Assertions.assertTrue(result.isPresent(), "잔액 정보가 존재해야 합니다.");

            BigDecimal finalBalance = result.get().getAmount();
            BigDecimal expectedBalance = INITIAL_AMOUNT.subtract(DEDUCT_AMOUNT);

            log.warn("▶ 최종 잔액: {}", finalBalance.stripTrailingZeros().toPlainString());
            log.warn("▶ 예상 최종 잔액: {}", expectedBalance.stripTrailingZeros().toPlainString());
            log.warn("▶ 성공한 차감 횟수: {}", successCount.get());

            Assertions.assertEquals(1, successCount.get(), "차감은 한 번만 성공해야 합니다.");
            Assertions.assertEquals(expectedBalance.stripTrailingZeros().toPlainString(), finalBalance.stripTrailingZeros().toPlainString(), "차감된 금액이 예상과 다릅니다.");
        }
    }

    @Nested
    class 잔액_순차_요청_정합성_테스트  {

        private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(500);
        private static final BigDecimal CHARGE_AMOUNT = BigDecimal.valueOf(1000);
        private static final BigDecimal EXPECTED_BALANCE_AFTER_BOTH = BigDecimal.valueOf(500); // 충전 -> 차감
        private static final BigDecimal EXPECTED_BALANCE_AFTER_CHARGE_ONLY = BigDecimal.valueOf(1500); // 차감 실패 -> 충전

        @Test
        void 충전_후_차감_순서로_요청하면_정상적으로_잔액이_감소한다() {
            // given
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));

            // when
            balanceService.chargeBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));
            balanceService.deductBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));

            // then
            BigDecimal result = balanceRepository.findByUserId(USER_ID).get().getAmount();
            String formattedAmount = result.stripTrailingZeros().toPlainString();

            log.info("최종 잔액: {}", formattedAmount);

            if (result.compareTo(EXPECTED_BALANCE_AFTER_BOTH) == 0) {
                log.info("정상 케이스 1 - 충전 후 차감이 성공한 시나리오 (잔액: {})", formattedAmount);
            } else {
                log.warn("예기치 않은 결과 - 잔액: {}", formattedAmount);
            }

            Assertions.assertEquals(EXPECTED_BALANCE_AFTER_BOTH.stripTrailingZeros().toPlainString(),
                    result.stripTrailingZeros().toPlainString(),
                    "충전 후 차감 시 잔액이 기대와 다릅니다.");
        }

        @Test
        void 차감_후_충전_순서로_요청하면_차감은_실패하고_충전만_성공한다() {
            // given
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));

            // when
            Assertions.assertThrows(CustomException.class, () ->
                    balanceService.deductBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT))
            );
            balanceService.chargeBalance(new BalanceCommand(USER_ID, CHARGE_AMOUNT));

            // then
            BigDecimal result = balanceRepository.findByUserId(USER_ID).get().getAmount();
            String formattedAmount = result.stripTrailingZeros().toPlainString();

            log.info("최종 잔액: {}", formattedAmount);

            if (result.compareTo(EXPECTED_BALANCE_AFTER_CHARGE_ONLY) == 0) {
                log.info("정상 케이스 2 - 차감 실패 후 충전만 성공한 시나리오 (잔액: {})", formattedAmount);
            } else {
                log.warn("예기치 않은 결과 - 잔액: {}", formattedAmount);
            }

            Assertions.assertEquals(EXPECTED_BALANCE_AFTER_CHARGE_ONLY.stripTrailingZeros().toPlainString(),
                    formattedAmount,
                    "차감 실패 후 충전만 성공한 경우 잔액이 기대와 다릅니다.");
        }
    }

    @Nested
    class 충전_차감_동시_요청 {

        /**
         * 현재 잔액이 500원일 때,
         * 충전과 차감을 동시에 요청하면 실행 순서에 따라 다음 두 가지 정상 결과 중 하나가 발생한다.
         * <p>
         * 1. 충전이 먼저 → 차감도 성공 → 최종 잔액: 500원
         * 2. 차감이 먼저 → 실패 + 충전만 성공 → 최종 잔액: 1500원
         * <p>
         * 이 외의 결과는 중복 처리 또는 동시성 문제로 간주한다.
         */
        private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(500);
        private static final BigDecimal AMOUNT = BigDecimal.valueOf(1000);

        @BeforeEach
        void initBalance() {
            balanceRepository.save(UserBalance.create(USER_ID, INITIAL_BALANCE));
        }

        @Test
        void 충전과_차감을_랜덤_순서로_요청하면_정합성이_유지되어야_한다() throws InterruptedException {
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(CONCURRENCY_THREADS);
            AtomicInteger chargeSuccess = new AtomicInteger();
            AtomicInteger deductSuccess = new AtomicInteger();

            Runnable chargeTask = () -> {
                try {
                    startSignal.await();
                    balanceService.chargeBalance(new BalanceCommand(USER_ID, AMOUNT));
                    chargeSuccess.incrementAndGet();
                } catch (Exception e) {
                    log.warn("충전 실패: {}", e.getMessage());
                } finally {
                    doneSignal.countDown();
                }
            };

            Runnable deductTask = () -> {
                try {
                    startSignal.await();
                    balanceService.deductBalance(new BalanceCommand(USER_ID, AMOUNT));
                    deductSuccess.incrementAndGet();
                } catch (Exception e) {
                    log.warn("차감 실패: {}", e.getMessage());
                } finally {
                    doneSignal.countDown();
                }
            };

            ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY_THREADS);

            List<Runnable> tasks = new ArrayList<>(List.of(chargeTask, deductTask));
            Collections.shuffle(tasks); // 매번 다른 순서

            tasks.forEach(executor::submit);

            startSignal.countDown();
            doneSignal.await();

            BigDecimal finalBalance = balanceRepository.findByUserId(USER_ID)
                    .orElseThrow(() -> new IllegalStateException("잔액 정보 없음"))
                    .getAmount();

            int charge = chargeSuccess.get();
            int deduct = deductSuccess.get();

            log.info("충전 성공 횟수: {}", charge);
            log.info("차감 성공 횟수: {}", deduct);
            log.info("최종 잔액: {}", finalBalance.stripTrailingZeros().toPlainString());

            boolean case1 = charge == 1 && deduct == 1 && finalBalance.compareTo(INITIAL_BALANCE) == 0;
            boolean case2 = charge == 1 && deduct == 0 && finalBalance.compareTo(INITIAL_BALANCE.add(AMOUNT)) == 0;

            Assertions.assertTrue(case1 || case2,
                    String.format("정합성 실패 - 충전: %d, 차감: %d, 잔액: %s", charge, deduct, finalBalance.stripTrailingZeros().toPlainString()));
        }
    }
}
