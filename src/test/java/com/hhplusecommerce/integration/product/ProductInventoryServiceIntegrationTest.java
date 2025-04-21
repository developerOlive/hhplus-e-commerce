package com.hhplusecommerce.integration.product;

import com.hhplusecommerce.IntegrationTestSupport;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.domain.product.*;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProductInventoryServiceIntegrationTest extends IntegrationTestSupport {

    private static final BigDecimal PRICE_SERUM = BigDecimal.valueOf(10_000);
    private static final BigDecimal PRICE_MASK = BigDecimal.valueOf(2_000);
    private static final BigDecimal PRICE_EMULSION = BigDecimal.valueOf(9_000);
    private static final BigDecimal PRICE_CLEANSING = BigDecimal.valueOf(7_000);
    private static final BigDecimal PRICE_HAND_CREAM = BigDecimal.valueOf(5_000);
    private static final BigDecimal PRICE_LOTION = BigDecimal.valueOf(8_000);
    private static final BigDecimal PRICE_CUSHION = BigDecimal.valueOf(15_000);
    private static final BigDecimal PRICE_ESSENCE = BigDecimal.valueOf(12_000);

    private static final int STOCK_FULL = 10;
    private static final int STOCK_HALF = 5;
    private static final int STOCK_FEW = 2;
    private static final int STOCK_EIGHT = 8;
    private static final int QUANTITY_DEDUCT = 3;

    @Autowired
    private ProductInventoryService productInventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Nested
    class 단일상품_검증과_차감 {

        @Test
        void 재고가_충분한_경우_예외가_발생하지_않는다() {
            Product product = saveProduct("핸드크림", "생활용품", PRICE_HAND_CREAM);
            saveInventory(product, STOCK_FULL);

            productInventoryService.validateProductStock(product.getId(), 5);
        }

        @Test
        void 재고가_부족하면_예외가_발생한다() {
            Product product = saveProduct("로션", "화장품", PRICE_LOTION);
            saveInventory(product, STOCK_FEW);

            assertThatThrownBy(() -> productInventoryService.validateProductStock(product.getId(), 5))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void 재고를_정상적으로_차감한다() {
            Product product = saveProduct("쿠션", "화장품", PRICE_CUSHION);
            saveInventory(product, STOCK_FULL);

            productInventoryService.decreaseStock(product.getId(), QUANTITY_DEDUCT);

            ProductInventory updated = productInventoryRepository.findInventoryByProductId(product.getId()).get();
            assertThat(updated.getStock()).isEqualTo(STOCK_FULL - QUANTITY_DEDUCT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void 재고_차감시_음수_또는_0이면_예외가_발생한다(int invalidQuantity) {
            Product product = saveProduct("에센스", "화장품", PRICE_ESSENCE);
            saveInventory(product, STOCK_FULL);

            assertThatThrownBy(() -> productInventoryService.decreaseStock(product.getId(), invalidQuantity))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INVALID_STOCK_AMOUNT.getMessage());
        }
    }

    @Nested
    class 여러상품_검증과_차감 {

        @Test
        void 여러_상품의_재고가_충분한_경우_예외가_발생하지_않는다() {
            Product p1 = saveProduct("세럼", "화장품", PRICE_SERUM);
            Product p2 = saveProduct("마스크팩", "화장품", PRICE_MASK);
            saveInventory(p1, STOCK_FULL);
            saveInventory(p2, STOCK_HALF);

            List<OrderItemCommand> items = List.of(
                    new OrderItemCommand(p1.getId(), 2, p1.getPrice()),
                    new OrderItemCommand(p2.getId(), 1, p2.getPrice())
            );

            productInventoryService.validateAllProductStocks(items);
        }

        @Test
        void 여러_상품의_재고가_정확히_차감된다() {
            Product p1 = saveProduct("에멀전", "화장품", PRICE_EMULSION);
            Product p2 = saveProduct("클렌징폼", "화장품", PRICE_CLEANSING);
            saveInventory(p1, STOCK_FULL);
            saveInventory(p2, STOCK_EIGHT);

            List<OrderItem> orderItems = List.of(
                    new OrderItem(null, p1.getId(), 3, p1.getPrice()),
                    new OrderItem(null, p2.getId(), 5, p2.getPrice())
            );

            productInventoryService.decreaseStocks(orderItems);

            ProductInventory i1 = productInventoryRepository.findInventoryByProductId(p1.getId()).get();
            ProductInventory i2 = productInventoryRepository.findInventoryByProductId(p2.getId()).get();

            assertAll(
                    () -> assertThat(i1.getStock()).isEqualTo(7),
                    () -> assertThat(i2.getStock()).isEqualTo(3)
            );
        }
    }

    private Product saveProduct(String name, String category, BigDecimal price) {
        Product product = Instancio.of(Product.class)
                .ignore(field("inventory"))
                .set(field("name"), name)
                .set(field("category"), category)
                .set(field("price"), price)
                .create();
        return productRepository.save(product);
    }

    private ProductInventory saveInventory(Product product, int stock) {
        Product savedProduct = productRepository.save(product);
        ProductInventory inventory = ProductInventory.builder()
                .productId(savedProduct.getId())
                .stock(stock)
                .build();
        savedProduct.setInventory(inventory);
        return productInventoryRepository.save(inventory);
    }
}
