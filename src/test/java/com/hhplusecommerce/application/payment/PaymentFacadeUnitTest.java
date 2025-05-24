package com.hhplusecommerce.application.payment;

import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.domain.order.event.OrderEventPublisher;
import com.hhplusecommerce.domain.payment.Payment;
import com.hhplusecommerce.domain.payment.PaymentCommand;
import com.hhplusecommerce.domain.payment.PaymentService;
import com.hhplusecommerce.domain.payment.PaymentStatus;
import com.hhplusecommerce.domain.product.ProductInventoryService;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.hhplusecommerce.domain.payment.PaymentMethod.CREDIT_CARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock private OrderEventPublisher orderEventPublisher;
    @Mock private CouponService couponService;

    @Captor
    private ArgumentCaptor<OrderEvent.Completed> eventCaptor;

    @Test
    void 결제를_정상적으로_완료하면_이벤트가_발행된다() {
        // given
        PaymentCommand command = new PaymentCommand(ORDER_ID, CREDIT_CARD);
        Order order = mock(Order.class);
        List<OrderItem> orderItemList = List.of(mock(OrderItem.class));
        OrderItems orderItems = new OrderItems(orderItemList);

        when(orderService.getOrder(ORDER_ID)).thenReturn(order);
        when(order.getId()).thenReturn(ORDER_ID);
        when(order.getUserId()).thenReturn(USER_ID);
        when(order.getFinalAmount()).thenReturn(FINAL_AMOUNT);
        when(order.getOrderItems()).thenReturn(orderItems);

        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(ORDER_ID);
        when(payment.getPaidAmount()).thenReturn(FINAL_AMOUNT);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(paymentService.completePayment(ORDER_ID, FINAL_AMOUNT)).thenReturn(payment);

        // when
        PaymentResult result = paymentFacade.completePayment(command);

        // then
        verify(balanceService).deductBalance(any(BalanceCommand.class));
        verify(inventoryService).decreaseStocks(eq(orderItemList));
        verify(order).complete();
        verify(couponService).useCoupon(order);
        verify(paymentService).completePayment(ORDER_ID, FINAL_AMOUNT);
        verify(orderEventPublisher).publish(eventCaptor.capture());

        assertThat(result.paymentId()).isEqualTo(ORDER_ID);
        assertThat(result.paidAmount()).isEqualTo(FINAL_AMOUNT);
        assertThat(eventCaptor.getValue().orderItems().getItems()).hasSize(orderItemList.size());
    }

    @Test
    void 잔액이_부족하면_결제가_실패하고_이벤트가_발행되지_않는다() {
        // given
        PaymentCommand command = new PaymentCommand(ORDER_ID, CREDIT_CARD);
        Order order = mock(Order.class);
        OrderItems orderItems = new OrderItems(List.of());

        when(orderService.getOrder(ORDER_ID)).thenReturn(order);
        when(order.getUserId()).thenReturn(USER_ID);
        when(order.getFinalAmount()).thenReturn(FINAL_AMOUNT);
        when(order.getOrderItems()).thenReturn(orderItems);

        doThrow(new CustomException(ErrorType.INSUFFICIENT_BALANCE))
                .when(balanceService).deductBalance(any());

        // when & then
        assertThatThrownBy(() -> paymentFacade.completePayment(command))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());

        verify(inventoryService).decreaseStocks(any());
        verify(paymentService, never()).completePayment(anyLong(), any());
        verify(orderEventPublisher, never()).publish(any());
    }
}
