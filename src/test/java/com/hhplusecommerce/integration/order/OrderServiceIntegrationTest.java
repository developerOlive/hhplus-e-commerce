package com.hhplusecommerce.integration.order;

import com.hhplusecommerce.domain.order.*;
import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class OrderServiceIntegrationTest extends IntegrationTestSupport {

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final BigDecimal PRICE = new BigDecimal("1000");
    private static final String PRODUCT_CATEGORY = "electronics";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Nested
    class createOrder_주문_생성 {

        private static final int VALID_QUANTITY = 2;

        @Test
        void 주문을_생성하면_Order와_OrderItem이_저장된다() {
            // given
            OrderItemCommand item = new OrderItemCommand(PRODUCT_ID, VALID_QUANTITY, PRICE, PRODUCT_CATEGORY);
            OrderCommand command = new OrderCommand(USER_ID, null, List.of(item));

            BigDecimal totalAmount = command.orderItems().stream()
                    .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal finalAmount = totalAmount.subtract(discountAmount);

            // when
            Long orderId = orderService.createOrder(command, discountAmount);

            // then
            Order order = orderRepository.findById(orderId).orElseThrow();
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

            assertAll(
                    () -> assertThat(order.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT),
                    () -> assertThat(order.getTotalAmount()).isEqualTo(totalAmount),
                    () -> assertThat(order.getFinalAmount()).isEqualTo(finalAmount),
                    () -> assertThat(items).hasSize(1),
                    () -> assertThat(items.get(0).getProductId()).isEqualTo(PRODUCT_ID)
            );
        }

        @Test
        void 할인된_금액으로_주문이_생성된다() {
            // given
            OrderItemCommand item = new OrderItemCommand(PRODUCT_ID, 3, PRICE, PRODUCT_CATEGORY);
            OrderCommand command = new OrderCommand(USER_ID, null, List.of(item));

            BigDecimal totalAmount = PRICE.multiply(BigDecimal.valueOf(3));
            BigDecimal discountAmount = new BigDecimal("500");
            BigDecimal finalAmount = totalAmount.subtract(discountAmount);

            // when
            Long orderId = orderService.createOrder(command, discountAmount);

            // then
            Order order = orderRepository.findById(orderId).orElseThrow();
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

            assertAll(
                    () -> assertThat(order.getTotalAmount()).isEqualByComparingTo(totalAmount),
                    () -> assertThat(order.getFinalAmount()).isEqualByComparingTo(finalAmount),
                    () -> assertThat(items).hasSize(1)
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 수량이_0또는_음수이면_예외가_발생한다(int invalidQuantity) {
            // given
            OrderItemCommand item = new OrderItemCommand(PRODUCT_ID, invalidQuantity, PRICE, PRODUCT_CATEGORY);
            OrderCommand command = new OrderCommand(USER_ID, null, List.of(item));

            BigDecimal discountAmount = BigDecimal.ZERO;

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command, discountAmount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_STOCK_AMOUNT.getMessage());
        }
    }

    @Nested
    class completeOrder_주문_완료처리 {

        @Test
        void 결제대기_상태인_주문을_완료처리하면_상태가_변경된다() {
            // given
            Long orderId = createTestOrder();

            // when
            orderService.completeOrder(orderId);

            // then
            Order updated = orderRepository.findById(orderId).orElseThrow();
            assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        void 이미_완료된_주문은_다시_완료처리할_수_없다_예외가_발생한다() {
            // given
            Long orderId = createTestOrder();
            orderService.completeOrder(orderId);

            // when & then
            assertThatThrownBy(() -> orderService.completeOrder(orderId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_COMPLETE.getMessage());
        }
    }

    private Long createTestOrder() {
        OrderItemCommand item = new OrderItemCommand(PRODUCT_ID, 1, PRICE, PRODUCT_CATEGORY);
        OrderCommand command = new OrderCommand(USER_ID, null, List.of(item));

        BigDecimal discountAmount = BigDecimal.ZERO;

        Long orderId = orderService.createOrder(command, discountAmount);

        jdbcTemplate.update("UPDATE `order` SET created_at = ? WHERE id = ?",
                LocalDateTime.now().minusMinutes(31), orderId);

        return orderId;
    }
}
