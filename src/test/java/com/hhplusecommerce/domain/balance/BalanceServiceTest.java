package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.hhplusecommerce.domain.balance.BalanceChangeType.DEDUCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    private static final Long USER_ID = 1L;

    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @Nested
    class 잔액_조회_테스트 {
        private static final Long UNKNOWN_USER_ID = 2L;

        @Test
        void 유저의_잔액을_정상적으로_조회한다() {
            // given
            UserBalance userBalance = UserBalance.initialize(USER_ID);
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when
            BalanceResult result = balanceService.getBalance(USER_ID);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.balance()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void 잔액_정보가_없으면_예외가_발생한다() {
            // given
            when(balanceRepository.findByUserId(UNKNOWN_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> balanceService.getBalance(UNKNOWN_USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.USER_BALANCE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 잔액_충전_테스트 {

        private static final BigDecimal CHARGE_AMOUNT = BigDecimal.valueOf(2000);
        private static final Long USER_ID = 1L;

        @Test
        void 유저의_잔액에_금액을_충전한다() {
            // given
            UserBalance userBalance = UserBalance.initialize(USER_ID);
            when(balanceRepository.findOrCreateById(USER_ID)).thenReturn(userBalance);

            // when
            BalanceCommand balanceCommand = new BalanceCommand(USER_ID, CHARGE_AMOUNT);
            BalanceResult result = balanceService.chargeBalance(balanceCommand);

            // then
            assertThat(result.balance()).isEqualTo(CHARGE_AMOUNT);
            verify(balanceHistoryRepository).save(any());
        }

        @Test
        void 충전_금액이_음수이면_예외가_발생한다() {
            // given: 음수 충전 금액을 시도할 때
            UserBalance userBalance = UserBalance.initialize(USER_ID);
            when(balanceRepository.findOrCreateById(USER_ID)).thenReturn(userBalance);

            // when & then: 예외 발생 확인
            assertThatThrownBy(() -> balanceService.chargeBalance(new BalanceCommand(USER_ID, BigDecimal.valueOf(-100))))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
        }
    }

    @Nested
    class 잔액_차감_테스트 {

        private static final BigDecimal INITIAL_AMOUNT = BigDecimal.valueOf(1000);
        private static final BigDecimal DEDUCT_AMOUNT = BigDecimal.valueOf(500);
        private static final Long USER_ID = 1L;

        @Test
        void 잔액을_정상적으로_차감한다() {
            // given
            UserBalance userBalance = UserBalance.builder()
                    .userId(USER_ID)
                    .amount(INITIAL_AMOUNT)
                    .build();

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when
            BalanceResult result = balanceService.deductBalance(new BalanceCommand(USER_ID, DEDUCT_AMOUNT));

            // then
            assertThat(result.balance()).isEqualTo(INITIAL_AMOUNT.subtract(DEDUCT_AMOUNT));
            verify(balanceHistoryRepository).save(any());
        }

        @Test
        void 기존_잔액과_차감_금액이_같으면_0원이고_예외없이_이력이_생성된다() {
            // given
            BigDecimal same = BigDecimal.valueOf(1000);
            UserBalance userBalance = UserBalance.builder()
                    .userId(USER_ID)
                    .amount(same)
                    .build();

            // when
            BalanceHistory history = BalanceHistory.create(userBalance.getUserId(), same, same, DEDUCT);

            // then
            assertThat(history.getAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void 잔액이_부족하면_예외가_발생한다() {
            // given
            UserBalance userBalance = UserBalance.initialize(USER_ID);
            userBalance.charge(INITIAL_AMOUNT);
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when & then
            assertThatThrownBy(() -> balanceService.deductBalance(new BalanceCommand(USER_ID, BigDecimal.valueOf(2000))))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }
    }

}
