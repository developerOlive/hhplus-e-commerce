package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long EXISTING_ORDER_ID = 1L;
    private static final Long NON_EXISTENT_ORDER_ID = 999L;
    private static final Long USER_ID = 123L;
    private static final Long COUPON_ID = 456L;
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(5000);
    private static final int ITEM_QUANTITY = 2;
    private static final BigDecimal TOTAL_AMOUNT = ITEM_PRICE.multiply(BigDecimal.valueOf(ITEM_QUANTITY));
    private static final BigDecimal DISCOUNT_AMOUNT = BigDecimal.valueOf(1000);
    private static final BigDecimal FINAL_AMOUNT = TOTAL_AMOUNT.subtract(DISCOUNT_AMOUNT);

    @InjectMocks
    private OrderService orderService;

    @Mock private OrderRepository orderRepository;

    private Order mockOrder;

    @BeforeEach
    void setUp() {
        OrderCommand command = new OrderCommand(USER_ID, COUPON_ID, List.of(
                new OrderItemCommand(1L, ITEM_QUANTITY, ITEM_PRICE)
        ));
        mockOrder = Order.create(command);
        mockOrder.applyFinalAmount(FINAL_AMOUNT);
        ReflectionTestUtils.setField(mockOrder, "orderStatus", OrderStatus.PAYMENT_WAIT);
    }

    @Test
    void 주문이_성공적으로_생성된다() {
        // Given
        OrderCommand command = new OrderCommand(USER_ID, COUPON_ID, List.of(
                new OrderItemCommand(1L, ITEM_QUANTITY, ITEM_PRICE)
        ));

        // When
        Long orderId = orderService.createOrder(command, DISCOUNT_AMOUNT);

        // Then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();
        assertThat(orderId).isEqualTo(mockOrder.getId());
        assertThat(savedOrder.getUserId()).isEqualTo(USER_ID);
        assertThat(savedOrder.getCouponIssueId()).isEqualTo(COUPON_ID);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
        assertThat(savedOrder.getFinalAmount()).isEqualByComparingTo(FINAL_AMOUNT);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);
        assertThat(savedOrder.getOrderItems()).hasSize(1);
        assertThat(savedOrder.getOrderItems().get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_주문ID로_조회할_때_예외가_발생한다() {
        // Given
        lenient().when(orderRepository.findByIdWithItems(NON_EXISTENT_ORDER_ID))
                .thenReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.getOrder(NON_EXISTENT_ORDER_ID));

        // Then
        assertEquals(ErrorType.ORDER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void 결제대기_상태의_주문이_성공적으로_완료된다() {
        // Given
        when(orderRepository.findById(EXISTING_ORDER_ID)).thenReturn(Optional.of(mockOrder));

        // When
        orderService.completeOrder(EXISTING_ORDER_ID);

        // Then
        verify(orderRepository, times(1)).findById(EXISTING_ORDER_ID);
        assertEquals(OrderStatus.COMPLETED, mockOrder.getOrderStatus());
    }
}
