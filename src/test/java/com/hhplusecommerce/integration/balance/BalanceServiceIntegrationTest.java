package com.hhplusecommerce.integration.balance;

import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.domain.balance.*;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;

class BalanceServiceIntegrationTest extends IntegrationTestSupport {

    private static final BigDecimal INITIAL_AMOUNT = BigDecimal.valueOf(10_000);
    private static final BigDecimal DEDUCT_AMOUNT = BigDecimal.valueOf(3_000);
    private static final BigDecimal EXPECTED_BALANCE_AFTER_DEDUCTION = BigDecimal.valueOf(7_000);
    private static final BigDecimal INSUFFICIENT_BALANCE = BigDecimal.valueOf(1_000);

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    private Long saveUserWithInitialBalance(BigDecimal amount) {
        UserBalance userBalance = Instancio.of(UserBalance.class)
                .set(field(UserBalance::getAmount), amount)
                .create();
        return balanceRepository.save(userBalance).getUserId();
    }

    @Nested
    class 잔액_조회 {

        @Test
        void 잔액이_존재하면_정상적으로_조회된다() {
            Long userId = saveUserWithInitialBalance(INITIAL_AMOUNT);
            BalanceResult result = balanceService.getBalance(userId);

            assertAll(
                    () -> assertThat(result.userId()).isEqualTo(userId),
                    () -> assertThat(result.balance()).isEqualByComparingTo(INITIAL_AMOUNT)
            );
        }

        @Test
        void 잔액이_없으면_예외가_발생한다() {
            assertThatThrownBy(() -> balanceService.getBalance(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.USER_BALANCE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 잔액_충전 {

        @Test
        void 잔액을_충전하면_금액이_추가되고_이력이_저장된다() {
            Long userId = saveUserWithInitialBalance(BigDecimal.ZERO);
            BalanceResult result = balanceService.chargeBalance(new BalanceCommand(userId, INITIAL_AMOUNT));
            List<BalanceHistory> histories = balanceHistoryRepository.findByUserId(userId);

            assertAll(
                    () -> assertThat(result.userId()).isEqualTo(userId),
                    () -> assertThat(result.balance()).isEqualByComparingTo(INITIAL_AMOUNT),
                    () -> assertThat(histories).hasSize(1),
                    () -> assertThat(histories.get(0).getChangeType()).isEqualTo(BalanceChangeType.CHARGE),
                    () -> assertThat(histories.get(0).getAfterBalance()).isEqualByComparingTo(INITIAL_AMOUNT)
            );
        }
    }

    @Nested
    class 잔액_차감 {

        @Test
        void 충분한_잔액이_있으면_정상적으로_차감되고_이력이_저장된다() {
            Long userId = saveUserWithInitialBalance(INITIAL_AMOUNT);
            balanceService.deductBalance(new BalanceCommand(userId, DEDUCT_AMOUNT));

            BalanceResult result = balanceService.getBalance(userId);
            List<BalanceHistory> histories = balanceHistoryRepository.findByUserId(userId);

            assertAll(
                    () -> assertThat(result.userId()).isEqualTo(userId),
                    () -> assertThat(result.balance()).isEqualByComparingTo(EXPECTED_BALANCE_AFTER_DEDUCTION),
                    () -> assertThat(histories).hasSize(1),
                    () -> assertThat(histories.get(0).getChangeType()).isEqualTo(BalanceChangeType.DEDUCT),
                    () -> assertThat(histories.get(0).getAfterBalance()).isEqualByComparingTo(EXPECTED_BALANCE_AFTER_DEDUCTION)
            );
        }

        @Test
        void 잔액이_부족하면_예외가_발생한다() {
            Long userId = saveUserWithInitialBalance(INSUFFICIENT_BALANCE);
            assertThatThrownBy(() -> balanceService.deductBalance(new BalanceCommand(userId, DEDUCT_AMOUNT)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-100"})
        void 차감_요청_금액이_0_또는_음수면_예외가_발생한다(String input) {
            Long userId = saveUserWithInitialBalance(INITIAL_AMOUNT);
            BigDecimal invalidAmount = new BigDecimal(input);

            assertThatThrownBy(() -> balanceService.deductBalance(new BalanceCommand(userId, invalidAmount)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
        }
    }

    @Nested
    class 잔액_유호성_검증 {

        private static final BigDecimal REQUIRED_AMOUNT = BigDecimal.valueOf(5_000);

        @Test
        void 충분한_잔액이면_검증에_성공한다() {
            Long userId = saveUserWithInitialBalance(INITIAL_AMOUNT);
            balanceService.validateEnough(userId, REQUIRED_AMOUNT);
        }

        @Test
        void 부족한_잔액이면_예외가_발생한다() {
            Long userId = saveUserWithInitialBalance(INSUFFICIENT_BALANCE);
            assertThatThrownBy(() -> balanceService.validateEnough(userId, REQUIRED_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-100"})
        void 요청_금액이_0_또는_음수면_예외가_발생한다(String input) {
            Long userId = saveUserWithInitialBalance(INITIAL_AMOUNT);
            BigDecimal invalidAmount = new BigDecimal(input);

            assertThatThrownBy(() -> balanceService.validateEnough(userId, invalidAmount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
        }
    }
}
