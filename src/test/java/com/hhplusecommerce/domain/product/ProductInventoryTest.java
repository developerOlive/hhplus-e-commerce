package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductInventoryTest {

    private ProductInventory productInventory;

    private static final long PRODUCT_ID = 1L;
    private static final int INITIAL_STOCK = 100;
    private static final int VALID_ADD_AMOUNT = 50;
    private static final int VALID_DEDUCT_AMOUNT = 30;
    private static final int EXCESS_DEDUCT_AMOUNT = 200;

    static ProductInventory 기본재고() {
        Product product = new Product("테스트상품", "카테고리", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
        return new ProductInventory(product, INITIAL_STOCK);
    }

    @BeforeEach
    void setUp() {
        productInventory = 기본재고();
    }

    @Nested
    class 재고_증가 {

        @Test
        void 정상적으로_재고를_증가시킨다() {
            productInventory.increase(VALID_ADD_AMOUNT);

            assertThat(productInventory.getStock()).isEqualTo(INITIAL_STOCK + VALID_ADD_AMOUNT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 음수_또는_0으로_증가하면_예외가_발생한다(int amount) {
            assertThatThrownBy(() -> productInventory.increase(amount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_STOCK_AMOUNT.getMessage());
        }
    }

    @Nested
    class 재고_차감 {

        @Test
        void 정상적으로_재고를_차감한다() {
            productInventory.decrease(VALID_DEDUCT_AMOUNT);

            assertThat(productInventory.getStock()).isEqualTo(INITIAL_STOCK - VALID_DEDUCT_AMOUNT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 수량이_0_이하이면_예외가_발생한다(int amount) {
            assertThatThrownBy(() -> productInventory.decrease(amount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_STOCK_AMOUNT.getMessage());
        }

        @Test
        void 재고보다_많은_수량을_차감하면_예외가_발생한다() {
            assertThatThrownBy(() -> productInventory.decrease(EXCESS_DEDUCT_AMOUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_STOCK.getMessage());
        }
    }

    @Nested
    class 재고_조회 {

        @Test
        void 재고를_정상적으로_조회한다() {
            int stock = productInventory.getStock();

            assertThat(stock).isEqualTo(INITIAL_STOCK);
        }
    }

    @Test
    void 초기_재고가_음수면_예외가_발생한다() {
        Product product = new Product("테스트상품", "카테고리", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        assertThatThrownBy(() -> new ProductInventory(product, -50))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorType.INVALID_STOCK_AMOUNT.getMessage());
    }
}
