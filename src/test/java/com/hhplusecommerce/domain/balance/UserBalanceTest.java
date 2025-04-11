package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserBalanceTest {

    private UserBalance userBalance;

    private static final Long USER_ID = 1L;
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100");
    private static final BigDecimal VALID_CHARGE_AMOUNT = new BigDecimal("50");
    private static final BigDecimal VALID_DEDUCT_AMOUNT = new BigDecimal("30");
    private static final BigDecimal EXCESS_DEDUCT_AMOUNT = new BigDecimal("200");

    static UserBalance 기본잔액() {
        return new UserBalance(USER_ID, INITIAL_BALANCE);
    }

    @BeforeEach
    void setUp() {
        userBalance = 기본잔액();
    }

    @Nested
    class 잔액_충전 {

        @Test
        void 잔액이_정상적으로_충전된다() {
            userBalance.charge(VALID_CHARGE_AMOUNT);

            assertThat(userBalance.getAmount()).isEqualTo(INITIAL_BALANCE.add(VALID_CHARGE_AMOUNT));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1"})
        void 잔액을_음수_또는_0으로_충전하면_예외가_발생한다(String amount) {
            assertThatThrownBy(() -> userBalance.charge(new BigDecimal(amount)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
        }
    }

    @Nested
    class 잔액_차감 {

        @Test
        void 잔액이_정상적으로_차감된다() {
            userBalance.deduct(VALID_DEDUCT_AMOUNT);

            assertThat(userBalance.getAmount()).isEqualTo(INITIAL_BALANCE.subtract(VALID_DEDUCT_AMOUNT));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1"})
        void 잔액을_음수_또는_0으로_차감하면_예외가_발생한다(String amount) {
            assertThatThrownBy(() -> userBalance.deduct(new BigDecimal(amount)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
        }

        @Test
        void 잔액보다_많은_금액을_차감하면_예외가_발생한다() {
            assertThatThrownBy(() -> userBalance.deduct(EXCESS_DEDUCT_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }
    }

    @Test
    void 초기_잔액이_음수면_예외가_발생한다() {
        assertThatThrownBy(() -> new UserBalance(USER_ID, new BigDecimal("-50.00")))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
    }
}
