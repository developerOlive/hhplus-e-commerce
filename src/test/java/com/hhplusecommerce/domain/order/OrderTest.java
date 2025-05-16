package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Long USER_ID = 1L;
    private static final Long COUPON_ISSUE_ID = 100L;
    private static final Long PRODUCT_ID = 1L;
    private static final int QUANTITY = 2;
    private static final BigDecimal PRICE_PER_ITEM = new BigDecimal("15000");
    private static final BigDecimal TOTAL_AMOUNT = PRICE_PER_ITEM.multiply(BigDecimal.valueOf(QUANTITY));
    private static final BigDecimal FINAL_AMOUNT = new BigDecimal("25000");
    private static final String CATEGORY = "electronics";

    private Order order;

    static Order 주문_생성(OrderStatus status) {
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(PRODUCT_ID, QUANTITY, PRICE_PER_ITEM, CATEGORY)
        );

        Order order = Order.create(
                new OrderCommand(USER_ID, COUPON_ISSUE_ID, items)
        );
        order.applyFinalAmount(FINAL_AMOUNT);
        ReflectionTestUtils.setField(order, "orderStatus", status);
        return order;
    }

    @BeforeEach
    void setUp() {
        order = 주문_생성(OrderStatus.PAYMENT_WAIT);
    }

    @Nested
    class 주문_생성_성공 {
        @Test
        void 주문이_정상적으로_생성된다() {
            assertThat(order.getUserId()).isEqualTo(USER_ID);
            assertThat(order.getCouponIssueId()).isEqualTo(COUPON_ISSUE_ID);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
            assertThat(order.getFinalAmount()).isEqualByComparingTo(FINAL_AMOUNT);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);
        }
    }

    @Nested
    class 주문_상태_변경_성공 {
        @Test
        void 결제_대기_상태에서_주문이_완료_상태로_변경된다() {
            order.complete();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        void 결제_대기_상태에서_주문이_만료상태로_변경된다() {
            order.expire();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
        }
    }

    @Nested
    class 주문_상태_변경_실패 {
        @Test
        void 주문_취소_상태에서_완료처리하면_예외가_발생한다() {
            Order canceledOrder = 주문_생성(OrderStatus.CANCELED);
            assertThatThrownBy(canceledOrder::complete)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_COMPLETE.getMessage());
        }

        @Test
        void 주문_완료_상태에서_만료처리하면_예외가_발생한다() {
            Order completedOrder = 주문_생성(OrderStatus.COMPLETED);
            assertThatThrownBy(completedOrder::expire)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_EXPIRE.getMessage());
        }
    }
}
