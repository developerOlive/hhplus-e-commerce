package com.hhplusecommerce.application.payment;

import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.payment.*;
import com.hhplusecommerce.domain.popularProduct.ProductSalesStatsService;
import com.hhplusecommerce.domain.product.ProductInventoryService;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.hhplusecommerce.domain.payment.PaymentMethod.CREDIT_CARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeUnitTest {

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final BigDecimal FINAL_AMOUNT = BigDecimal.valueOf(10000);

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock private BalanceService balanceService;
    @Mock private ProductInventoryService inventoryService;
    @Mock private OrderService orderService;
    @Mock private PaymentService paymentService;
    @Mock private ProductSalesStatsService productSalesStatsService;

    @Test
    void 결제를_정상적으로_완료한다() {
        // given
        PaymentCommand command = new PaymentCommand(ORDER_ID, CREDIT_CARD);
        Order order = mock(Order.class);
        List<OrderItem> orderItems = List.of(mock(OrderItem.class));

        when(order.getId()).thenReturn(ORDER_ID);
        when(order.getUserId()).thenReturn(USER_ID);
        when(order.getFinalAmount()).thenReturn(FINAL_AMOUNT);
        when(order.getOrderItems()).thenReturn(orderItems);
        when(orderService.getOrder(ORDER_ID)).thenReturn(order);

        Payment payment = new Payment(ORDER_ID, FINAL_AMOUNT, PaymentStatus.PENDING);
        when(paymentService.completePayment(ORDER_ID, FINAL_AMOUNT)).thenReturn(payment);

        // when
        PaymentResult result = paymentFacade.completePayment(command);

        // then
        verify(balanceService).deductBalance(any(BalanceCommand.class));
        verify(inventoryService).decreaseStocks(eq(orderItems));
        verify(order).complete();
        verify(paymentService).completePayment(ORDER_ID, FINAL_AMOUNT);
        verify(productSalesStatsService).recordSales(eq(orderItems), eq(LocalDate.now()));

        assertThat(result.paymentId()).isEqualTo(payment.getId());
        assertThat(result.paidAmount()).isEqualTo(payment.getPaidAmount());
    }

    @Test
    void 잔액이_부족하면_결제가_실패한다() {
        // given
        PaymentCommand command = new PaymentCommand(ORDER_ID, CREDIT_CARD);
        Order order = mock(Order.class);

        when(orderService.getOrder(ORDER_ID)).thenReturn(order);
        when(order.getUserId()).thenReturn(USER_ID);
        when(order.getFinalAmount()).thenReturn(FINAL_AMOUNT);

        doThrow(new CustomException(ErrorType.INSUFFICIENT_BALANCE))
                .when(balanceService).deductBalance(any(BalanceCommand.class));

        // when & then
        assertThatThrownBy(() -> paymentFacade.completePayment(command))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());

        verify(inventoryService, never()).decreaseStocks(any());
        verify(paymentService, never()).completePayment(anyLong(), any());
        verify(productSalesStatsService, never()).recordSales(any(), any());
    }
}
