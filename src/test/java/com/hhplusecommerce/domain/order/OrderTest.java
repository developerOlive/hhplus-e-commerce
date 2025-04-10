package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Long USER_ID = 1L;
    private static final Long COUPON_ISSUE_ID = 100L;
    private static final LocalDateTime ORDER_DATE = LocalDateTime.of(2025, 4, 10, 10, 0);
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("30000");
    private static final BigDecimal FINAL_AMOUNT = new BigDecimal("25000");

    private Order order;

    static Order 주문_생성(OrderStatus status) {
        return Order.builder()
                .userId(USER_ID)
                .couponIssueId(COUPON_ISSUE_ID)
                .orderDate(ORDER_DATE)
                .totalAmount(TOTAL_AMOUNT)
                .finalAmount(FINAL_AMOUNT)
                .status(status)
                .build();
    }

    @BeforeEach
    void setUp() {
        // 결제 대기 상태로 주문을 생성
        order = 주문_생성(OrderStatus.PAYMENT_WAIT);
    }

    @Nested
    class 주문_생성_성공 {

        @Test
        void 주문이_정상적으로_생성된다() {
            assertThat(order.getUserId()).isEqualTo(USER_ID);
            assertThat(order.getCouponIssueId()).isEqualTo(COUPON_ISSUE_ID);
            assertThat(order.getOrderDate()).isEqualTo(ORDER_DATE);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
            assertThat(order.getFinalAmount()).isEqualByComparingTo(FINAL_AMOUNT);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);
            assertThat(order.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(order.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Nested
    class 주문_상태_변경_성공 {

        @Test
        void 결제_대기_상태에서_주문이_완료_상태로_변경된다() {
            order.completeOrder();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        void 결제_대기_상태에서_주문이_취소_상태로_변경된다() {
            order.cancelOrder();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        void 결제_대기_상태에서_주문이_만료상태로_변경된다() {
            order = new Order(USER_ID, COUPON_ISSUE_ID, ORDER_DATE, TOTAL_AMOUNT, FINAL_AMOUNT, OrderStatus.PAYMENT_WAIT);
            order.expireOrder();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        }
    }

    @Nested
    class 주문_상태_변경_실패 {

        @Test
        void 결제_완료_상태에서_취소처리하면_예외가_발생한다() {
            // given
            order = new Order(USER_ID, COUPON_ISSUE_ID, ORDER_DATE, TOTAL_AMOUNT, FINAL_AMOUNT, OrderStatus.COMPLETED);

            // when & then
            assertThatThrownBy(order::cancelOrder)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_CANCEL.getMessage());
        }

        @Test
        void 결제_취소_상태에서_완료처리하면_예외가_발생한다() {
            // given
            order = new Order(USER_ID, COUPON_ISSUE_ID, ORDER_DATE, TOTAL_AMOUNT, FINAL_AMOUNT, OrderStatus.CANCELED);

            // when & then
            assertThatThrownBy(order::completeOrder)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_COMPLETE.getMessage());
        }

        @Test
        void 결제_만료_상태에서_완료처리하면_예외가_발생한다() {
            // given
            order = new Order(USER_ID, COUPON_ISSUE_ID, ORDER_DATE, TOTAL_AMOUNT, FINAL_AMOUNT, OrderStatus.EXPIRED);

            // when & then
            assertThatThrownBy(order::completeOrder)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_COMPLETE.getMessage());
        }

        @Test
        void 결제_완료_상태에서_만료처리하면_예외가_발생한다() {
            // given
            order = new Order(USER_ID, COUPON_ISSUE_ID, ORDER_DATE, TOTAL_AMOUNT, FINAL_AMOUNT, OrderStatus.COMPLETED);

            // when & then
            assertThatThrownBy(order::expireOrder)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_EXPIRE.getMessage());
        }

        @Test
        void 결제_취소_상태에서_만료처리하면_예외가_발생한다() {
            // given
            order = new Order(USER_ID, COUPON_ISSUE_ID, ORDER_DATE, TOTAL_AMOUNT, FINAL_AMOUNT, OrderStatus.CANCELED);

            // when & then
            assertThatThrownBy(order::expireOrder)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_ORDER_STATUS_TO_EXPIRE.getMessage());
        }

    }
}
