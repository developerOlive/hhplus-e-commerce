package com.hhplusecommerce.concurrency.product;

import com.hhplusecommerce.support.ConcurrencyTestSupport;
import com.hhplusecommerce.concurrency.ConcurrencyResult;
import com.hhplusecommerce.domain.product.*;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@Slf4j
class ProductInventoryConcurrencyTest extends ConcurrencyTestSupport {

    @Autowired
    ProductInventoryService inventoryService;

    @Autowired
    ProductInventoryRepository inventoryRepository;

    @Autowired
    ProductRepository productRepository;

    private static final int THREAD_COUNT = 2;
    private static final int INITIAL_STOCK_1 = 1;
    private static final int INITIAL_STOCK_2 = 12;
    private static final int ORDER_QUANTITY_A = 10;
    private static final int ORDER_QUANTITY_B = 5;

    @Nested
    class 시나리오1_동일상품_재고가_1개만_남은_경우_동시_주문 {

        private Product product;

        @BeforeEach
        void setUp() {
            product = productRepository.save(Instancio.of(Product.class)
                    .set(field("name"), "동시성테스트상품")
                    .set(field("category"), "테스트")
                    .set(field("price"), BigDecimal.valueOf(10000))
                    .create());

            inventoryRepository.save(ProductInventory.builder()
                    .product(product)
                    .stock(INITIAL_STOCK_1)
                    .build());
        }

        @Test
        void 동시에_주문시_1개만_성공하고_1개는_예외가_발생해야_한다() throws InterruptedException {
            long productId = product.getId();

            ConcurrencyResult result = executeWithLatch(THREAD_COUNT, r -> {
                try {
                    inventoryService.decreaseStock(productId, 1);
                    r.success();
                } catch (Exception e) {
                    r.error();
                }
            });

            ProductInventory updated = inventoryRepository.findInventoryByProductId(productId).orElseThrow();

            int expectedErrors = THREAD_COUNT - 1;

            log.warn("\uD83D\uDCE6 [시나리오1 결과 - 재고 1개 상품에 2명 동시 주문 → 중복 차감 발생 여부]");
            log.warn("▶ 초기 재고: {}", INITIAL_STOCK_1);
            log.warn("▶ 동시 요청 수: {}", THREAD_COUNT);
            log.warn("▶ 최종 재고: {}", updated.getStock());
            log.warn("▶ 기대 예외 발생 수: {}", expectedErrors);
            log.warn("▶ 실제 예외 발생 수: {}", result.getErrorCount());

            assertThat(updated.getStock()).isEqualTo(0);
            assertThat(result.getErrorCount()).isEqualTo(expectedErrors);
        }
    }

    @Nested
    class 시나리오2_재고는_12개인데_A는_10개_B는_5개를_동시에_주문하는_경우 {

        private Product product;

        @BeforeEach
        void setUp() {
            product = productRepository.save(Instancio.of(Product.class)
                    .set(field("name"), "재고초과테스트")
                    .set(field("category"), "테스트")
                    .set(field("price"), BigDecimal.valueOf(10000))
                    .create());

            inventoryRepository.save(ProductInventory.builder()
                    .product(product)
                    .stock(INITIAL_STOCK_2)
                    .build());
        }

        @Test
        void 재고보다_많은_총_수량이_동시에_주문되면_하나는_실패해야_한다() throws InterruptedException {
            long productId = product.getId();
            int[] orderQuantities = {ORDER_QUANTITY_A, ORDER_QUANTITY_B};

            ConcurrencyResult result = executeWithLatch(THREAD_COUNT, r -> {
                try {
                    int idx = ThreadLocalRandom.current().nextInt(0, 2);
                    inventoryService.decreaseStock(productId, orderQuantities[idx]);
                    r.success();
                } catch (Exception e) {
                    r.error();
                }
            });

            ProductInventory updated = inventoryRepository.findInventoryByProductId(productId).orElseThrow();

            log.warn("\uD83D\uDCE6 [시나리오2 결과 - 재고 12개 상품에 10개 & 5개 동시 주문 → 초과 주문 예외 발생 여부]");
            log.warn("▶ 초기 재고: {}", INITIAL_STOCK_2);
            log.warn("▶ 동시 요청 수: 2 (10개, 5개)");
            log.warn("▶ 최종 재고: {}", updated.getStock());
            log.warn("▶ 예외 발생 수: {}", result.getErrorCount());

            assertThat(result.getErrorCount()).isEqualTo(1);
        }
    }

    @Nested
    class 시나리오3_랜덤하게_다양한_수량_동시_주문 {

        private Product product;
        private static final int INITIAL_STOCK = 10;

        @BeforeEach
        void setUp() {
            product = productRepository.save(Instancio.of(Product.class)
                    .set(field("name"), "랜덤다량주문테스트")
                    .set(field("category"), "테스트")
                    .set(field("price"), BigDecimal.valueOf(10000))
                    .create());

            inventoryRepository.save(ProductInventory.builder()
                    .product(product)
                    .stock(INITIAL_STOCK)
                    .build());
        }

        @Test
        void 다양한_수량이_랜덤하게_들어와도_재고는_정확히_처리되어야_한다() throws InterruptedException {
            long productId = product.getId();
            int[] 주문수량들 = {4, 5, 3};
            List<Integer> 성공수량 = Collections.synchronizedList(new ArrayList<>());

            ConcurrencyResult result = executeWithLatch(주문수량들.length, r -> {
                int quantity = 주문수량들[ThreadLocalRandom.current().nextInt(주문수량들.length)];
                try {
                    inventoryService.decreaseStock(productId, quantity);
                    성공수량.add(quantity);
                    r.success();
                } catch (Exception e) {
                    r.error();
                }
            });

            ProductInventory updated = inventoryRepository.findInventoryByProductId(productId).orElseThrow();
            int 최종재고 = updated.getStock();
            int 차감된수량 = 성공수량.stream().mapToInt(Integer::intValue).sum();

            log.warn("▶ 초기 재고: {}", INITIAL_STOCK);
            log.warn("▶ 최종 재고: {}", 최종재고);
            log.warn("▶ 차감된 수량: {}", 차감된수량);
            log.warn("▶ 예외 발생 수: {}", result.getErrorCount());

            assertThat(차감된수량).isLessThanOrEqualTo(INITIAL_STOCK);
            assertThat(최종재고).isGreaterThanOrEqualTo(0);
        }
    }
}
