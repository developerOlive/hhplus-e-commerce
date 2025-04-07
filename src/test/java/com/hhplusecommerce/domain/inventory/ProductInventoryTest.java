package com.hhplusecommerce.domain.inventory;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductInventoryTest {

    private ProductInventory productInventory;
    private static final long PRODUCT_ID = 1L;
    private static final int INITIAL_QUANTITY = 100; // 초기 재고 수량
    private static final int AMOUNT_TO_ADD = 50;  // 추가할 수량
    private static final int AMOUNT_TO_DEDUCT = 30;  // 차감할 수량
    private static final int AMOUNT_TO_DEDUCT_EXCESS = 200; // 차감할 초과 수량2

    @BeforeEach
    void setUp() {
        productInventory = new ProductInventory(PRODUCT_ID, INITIAL_QUANTITY);  // 기본적으로 100개의 재고를 가진 상품 생성
    }

    @Nested
    class 재고_증가 {

        @Test
        void 정상적으로_수량을_증가시킨다() {
            // given & when
            productInventory.increaseQuantity(AMOUNT_TO_ADD);

            // then
            assertThat(productInventory.getQuantity()).isEqualTo(INITIAL_QUANTITY + AMOUNT_TO_ADD);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 음수_또는_0_수량으로_증가하면_InvalidQuantityException이_발생한다(int amount) {
            // given & when & then
            assertThatThrownBy(() -> productInventory.increaseQuantity(amount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_QUANTITY.getMessage());
        }
    }

    @Nested
    class 재고_차감 {

        @Test
        void 재고가_충분할_때_정상적으로_수량을_차감한다() {
            // given & when
            productInventory.decreaseQuantity(AMOUNT_TO_DEDUCT);

            // then
            assertThat(productInventory.getQuantity()).isEqualTo(INITIAL_QUANTITY - AMOUNT_TO_DEDUCT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 재고_차감_시_0이하_값으로_차감_하려_하면_InvalidQuantityException이_발생한다(int amountToDeduct) {
            // given & when & then
            assertThatThrownBy(() -> productInventory.decreaseQuantity(amountToDeduct))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_QUANTITY.getMessage());
        }

        @Test
        void 재고보다_많은_수량을_차감하려_하면_InsufficientInventoryException이_발생한다() {
            // given & when & then
            assertThatThrownBy(() -> productInventory.decreaseQuantity(AMOUNT_TO_DEDUCT_EXCESS))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_INVENTORY.getMessage());
        }
    }

    @Nested
    class 재고_조회 {

        @Test
        void 재고_수량을_정상적으로_조회한다() {
            // given & when
            int currentQuantity = productInventory.getQuantity();

            // then
            assertThat(currentQuantity).isEqualTo(INITIAL_QUANTITY);
        }
    }

    @Test
    void 초기_수량이_음수일_경우_0으로_설정된다() {
        // given & when
        ProductInventory inventory = new ProductInventory(1L, -50);

        // then
        assertThat(inventory.getQuantity()).isEqualTo(0);
    }
}
