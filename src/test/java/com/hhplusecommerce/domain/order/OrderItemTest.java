package com.hhplusecommerce.domain.order;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private static final Long PRODUCT_ID = 101L;
    private static final int QUANTITY = 2;
    private static final BigDecimal PRICE = new BigDecimal("100");
    private static final BigDecimal EXPECTED_TOTAL_AMOUNT = PRICE.multiply(BigDecimal.valueOf(QUANTITY));

    private Order mockOrder;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        mockOrder = Instancio.create(Order.class);
        orderItem = new OrderItem(mockOrder, PRODUCT_ID, QUANTITY, PRICE);
    }

    @Nested
    class 정상_케이스 {

        @Test
        void 주문항목이_정상적으로_생성된다() {
            assertThat(orderItem.getOrder()).isEqualTo(mockOrder);
            assertThat(orderItem.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(orderItem.getQuantity()).isEqualTo(QUANTITY);
            assertThat(orderItem.getPrice()).isEqualTo(PRICE);
            assertThat(orderItem.getTotalAmount()).isEqualTo(EXPECTED_TOTAL_AMOUNT);
        }

        @Test
        void totalAmount가_수량과_가격에_따라_정상적으로_계산된다() {
            assertThat(orderItem.getTotalAmount()).isEqualTo(EXPECTED_TOTAL_AMOUNT);
        }
    }

    @Nested
    class 주문_항목_생성_예외_처리 {

        @ParameterizedTest
        @MethodSource("quantitySource")
        void 수량이_음수일_경우_예외가_발생한다(int invalidQuantity) {
            assertThatThrownBy(() -> new OrderItem(mockOrder, PRODUCT_ID, invalidQuantity, PRICE))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_STOCK_AMOUNT.getMessage());
        }

        private static Stream<Arguments> quantitySource() {
            return Stream.of(
                    Arguments.of(-1),
                    Arguments.of(-100),
                    Arguments.of(0)
            );
        }

        @ParameterizedTest
        @MethodSource("invalidPriceSource")
        void 가격이_음수일_경우_예외가_발생한다(BigDecimal invalidPrice) {
            assertThatThrownBy(() -> new OrderItem(mockOrder, PRODUCT_ID, QUANTITY, invalidPrice))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_BALANCE_AMOUNT.getMessage());
        }

        private static Stream<Arguments> invalidPriceSource() {
            return Stream.of(
                    Arguments.of(new BigDecimal("-100")),
                    Arguments.of(new BigDecimal("-1")),
                    Arguments.of(new BigDecimal("0"))
            );
        }
    }
}
