package com.hhplusecommerce.domain.balance;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    private static final Long USER_ID = 1L;
    private static final BigDecimal INITIAL_AMOUNT = BigDecimal.valueOf(5000);

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
            UserBalance userBalance = UserBalance.initialize(USER_ID); // 기본 잔액은 0으로 초기화
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when
            BalanceCommand balanceCommand = new BalanceCommand(CHARGE_AMOUNT);
            BalanceResult result = balanceService.charge(USER_ID, balanceCommand);

            // then
            // 충전 후 잔액은 0 + CHARGE_AMOUNT 이므로 result.balance()는 CHARGE_AMOUNT와 같아야 한다
            assertThat(result.balance()).isEqualTo(CHARGE_AMOUNT);
            verify(balanceHistoryRepository).save(any());  // 잔액 변경 이력 저장 여부 확인
        }

        @Test
        void 충전_금액이_음수이면_예외가_발생한다() {
            // given
            UserBalance userBalance = UserBalance.initialize(USER_ID);
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when & then
            assertThatThrownBy(() -> balanceService.charge(USER_ID, new BalanceCommand(BigDecimal.valueOf(-100))))
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
            BigDecimal INITIAL_AMOUNT = BigDecimal.valueOf(1000);  // 초기 잔액을 1000으로 설정
            BigDecimal DEDUCT_AMOUNT = BigDecimal.valueOf(500);  // 차감할 금액
            UserBalance userBalance = UserBalance.initialize(USER_ID);  // 기본 잔액은 0
            userBalance.charge(INITIAL_AMOUNT);  // 잔액 1000으로 충전
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when
            BalanceResult result = balanceService.deduct(USER_ID, new BalanceCommand(DEDUCT_AMOUNT));

            // then
            assertThat(result.balance()).isEqualTo(INITIAL_AMOUNT.subtract(DEDUCT_AMOUNT));  // 잔액이 500으로 차감된 값 확인
            verify(balanceHistoryRepository).save(any());  // balanceHistoryRepository.save가 호출되었는지 확인
        }

        @Test
        void 잔액이_부족하면_예외가_발생한다() {
            // given
            UserBalance userBalance = UserBalance.initialize(USER_ID);
            userBalance.charge(INITIAL_AMOUNT);
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userBalance));

            // when & then
            assertThatThrownBy(() -> balanceService.deduct(USER_ID, new BalanceCommand(BigDecimal.valueOf(2000))))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }
    }

}
