package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long EXISTING_ORDER_ID = 1L;
    private static final Long NON_EXISTENT_ORDER_ID = 999L;
    private static final Long USER_ID = 123L;
    private static final Long COUPON_ID = 456L;
    private static final BigDecimal TOTAL_AMOUNT = BigDecimal.valueOf(10000);
    private static final BigDecimal FINAL_AMOUNT = BigDecimal.valueOf(9000);
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(5000);
    private static final int ITEM_QUANTITY = 2;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockOrder = Order.builder()
                .userId(USER_ID)
                .couponIssueId(COUPON_ID)
                .orderDate(LocalDateTime.now())
                .totalAmount(TOTAL_AMOUNT)
                .finalAmount(FINAL_AMOUNT)
                .status(OrderStatus.PAYMENT_WAIT)
                .build();
    }

    @Test
    void 주문이_성공적으로_생성된다() {
        // Given
        OrderCommand command = new OrderCommand(USER_ID, COUPON_ID, List.of(
                new OrderItemCommand(1L, ITEM_QUANTITY, ITEM_PRICE)
        ));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // When
        Long orderId = orderService.createOrder(command, TOTAL_AMOUNT, FINAL_AMOUNT);

        // Then
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void 존재하지_않는_주문ID로_조회할_때_예외가_발생한다() {
        // Given
        when(orderRepository.findById(NON_EXISTENT_ORDER_ID)).thenReturn(Optional.empty());

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
        assertEquals(OrderStatus.COMPLETED, mockOrder.getStatus());
    }
}
