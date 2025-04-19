package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceHistoryTest {

    private static final Long USER_ID = 1L;
    private static final BigDecimal BEFORE_BALANCE = new BigDecimal("100");
    private static final BigDecimal AFTER_BALANCE = new BigDecimal("150");
    private static final BalanceChangeType CHARGE = BalanceChangeType.CHARGE;

    static BalanceHistory 정상적인_잔액_변경_이력() {
        UserBalance userBalance = UserBalance.builder()
                .userId(USER_ID)
                .amount(BEFORE_BALANCE)
                .build();

        return BalanceHistory.create(userBalance, BEFORE_BALANCE, AFTER_BALANCE, CHARGE);
    }

    @Nested
    class 잔액_변경이력_생성_성공 {

        @Test
        void 정상적으로_잔액_변경_이력을_생성한다() {
            BalanceHistory history = 정상적인_잔액_변경_이력();

            BigDecimal expectedAmount = AFTER_BALANCE.subtract(BEFORE_BALANCE);

            assertThat(history.getUserBalance().getUserId()).isEqualTo(USER_ID);
            assertThat(history.getBeforeBalance()).isEqualTo(BEFORE_BALANCE);
            assertThat(history.getAfterBalance()).isEqualTo(AFTER_BALANCE);
            assertThat(history.getAmount()).isEqualTo(expectedAmount);
            assertThat(history.getChangeType()).isEqualTo(CHARGE);
            assertThat(history.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        void 기존_잔액과_차감하려는_잔액이_동일하면_변경액은_0이며_예외없이_생성된다() {
            UserBalance userBalance = UserBalance.builder()
                    .userId(USER_ID)
                    .amount(BEFORE_BALANCE)
                    .build();

            BalanceHistory history = BalanceHistory.create(userBalance, BEFORE_BALANCE, BEFORE_BALANCE, BalanceChangeType.CHARGE);

            assertThat(history.getAmount()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    class 잔액_변경이력_생성_실패 {

        @Test
        void changeType이_NULL이면_예외가_발생한다() {
            UserBalance userBalance = UserBalance.builder()
                    .userId(USER_ID)
                    .amount(BEFORE_BALANCE)
                    .build();

            assertThatThrownBy(() ->
                    BalanceHistory.create(userBalance, BEFORE_BALANCE, AFTER_BALANCE, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_CHANGE_TYPE.getMessage());
        }
    }

    @Nested
    class BalanceChangeType_Enum_유효성_검증 {

        @Test
        void 유효한_타입이면_true를_반환한다() {
            assertThat(BalanceChangeType.isValid("CHARGE")).isTrue();
            assertThat(BalanceChangeType.isValid("DEDUCT")).isTrue();
        }

        @Test
        void 존재하지_않는_타입이면_false를_반환한다() {
            assertThat(BalanceChangeType.isValid("INVALID_TYPE")).isFalse();
        }
    }
}
