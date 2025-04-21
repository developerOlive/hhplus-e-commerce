package com.hhplusecommerce.integration.order;

import com.hhplusecommerce.IntegrationTestSupport;
import com.hhplusecommerce.application.order.OrderFacade;
import com.hhplusecommerce.application.order.OrderResult;
import com.hhplusecommerce.domain.balance.BalanceRepository;
import com.hhplusecommerce.domain.balance.UserBalance;
import com.hhplusecommerce.domain.coupon.*;
import com.hhplusecommerce.domain.order.OrderCommand;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.domain.order.OrderStatus;
import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductInventory;
import com.hhplusecommerce.domain.product.ProductInventoryRepository;
import com.hhplusecommerce.domain.product.ProductRepository;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class OrderFacadeIntegrationTest extends IntegrationTestSupport {

    private static final Long USER_ID = 1L;
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(5000);
    private static final int QUANTITY = 2;
    private static final BigDecimal BALANCE_AMOUNT = BigDecimal.valueOf(100_000);
    private static final BigDecimal COUPON_DISCOUNT = BigDecimal.valueOf(2000);
    private static final int INITIAL_STOCK = 10;

    @Autowired private OrderFacade orderFacade;
    @Autowired private CouponRepository couponRepository;
    @Autowired private CouponHistoryRepository couponHistoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductInventoryRepository inventoryRepository;
    @Autowired private BalanceRepository balanceRepository;

    private Coupon coupon;
    private Product product;

    @BeforeEach
    void setUp() {
        product = productRepository.save(createProduct("립밤", "화장품", ITEM_PRICE));
        inventoryRepository.save(createInventory(product, INITIAL_STOCK));

        coupon = couponRepository.save(createCoupon(COUPON_DISCOUNT, 10));
        couponHistoryRepository.save(CouponHistory.issue(USER_ID, coupon));

        balanceRepository.save(UserBalance.create(USER_ID, BALANCE_AMOUNT));
    }

    @Nested
    class 주문_생성_성공 {

        @Test
        void 유효한_입력값으로_주문을_생성하면_정상적으로_반환된다() {
            Long couponIssueId = getFirstIssuedCoupon()
                    .orElseThrow(() -> new IllegalStateException("사용 가능한 쿠폰 없음"))
                    .getId();

            OrderItemCommand itemCommand = new OrderItemCommand(product.getId(), QUANTITY, ITEM_PRICE);
            OrderCommand command = new OrderCommand(USER_ID, couponIssueId, List.of(itemCommand));

            OrderResult result = orderFacade.placeOrder(command);

            BigDecimal expectedTotal = ITEM_PRICE.multiply(BigDecimal.valueOf(QUANTITY));
            BigDecimal expectedFinal = expectedTotal.subtract(COUPON_DISCOUNT);

            assertAll(
                    () -> assertThat(result.orderId()).isNotNull(),
                    () -> assertThat(result.totalAmount()).isEqualTo(expectedTotal),
                    () -> assertThat(result.finalAmount()).isEqualTo(expectedFinal),
                    () -> assertThat(result.orderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT)
            );
        }
    }

    @Nested
    class 주문_생성_실패 {

        @Test
        void 재고가_부족하면_예외가_발생한다() {
            inventoryRepository.findInventoryByProductId(product.getId()).ifPresent(inv -> inv.decrease(10));
            Long couponIssueId = getFirstIssuedCoupon()
                    .orElseThrow(() -> new IllegalStateException("사용 가능한 쿠폰 없음"))
                    .getId();

            OrderCommand command = new OrderCommand(USER_ID, couponIssueId,
                    List.of(new OrderItemCommand(product.getId(), QUANTITY, ITEM_PRICE)));

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_STOCK.getMessage());
        }

        @Test
        void 잔액이_부족하면_예외가_발생한다() {
            balanceRepository.findByUserId(USER_ID).ifPresent(balance -> balance.deduct(BALANCE_AMOUNT));
            Long couponIssueId = getFirstIssuedCoupon()
                    .orElseThrow(() -> new IllegalStateException("사용 가능한 쿠폰 없음"))
                    .getId();

            OrderCommand command = new OrderCommand(USER_ID, couponIssueId,
                    List.of(new OrderItemCommand(product.getId(), QUANTITY, ITEM_PRICE)));

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }

        @Nested
        class 쿠폰_검증_실패 {

            @Test
            void 쿠폰이_이미_사용된_경우_예외가_발생한다() {
                CouponHistory history = getFirstIssuedCoupon()
                        .orElseThrow(() -> new IllegalStateException("사용 가능한 쿠폰 없음"));
                history.use();

                OrderCommand command = new OrderCommand(USER_ID, history.getId(),
                        List.of(new OrderItemCommand(product.getId(), QUANTITY, ITEM_PRICE)));

                assertThatThrownBy(() -> orderFacade.placeOrder(command))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(ErrorType.COUPON_ALREADY_USED.getMessage());
            }
        }
    }

    private Optional<CouponHistory> getFirstIssuedCoupon() {
        return couponHistoryRepository.findCouponsByUserIdAndStatus(USER_ID, CouponUsageStatus.AVAILABLE)
                .stream()
                .findFirst();
    }

    private Product createProduct(String name, String category, BigDecimal price) {
        return Instancio.of(Product.class)
                .ignore(field("inventory"))
                .set(field("name"), name)
                .set(field("category"), category)
                .set(field("price"), price)
                .create();
    }

    private ProductInventory createInventory(Product product, int stock) {
        ProductInventory inventory = ProductInventory.builder()
                .productId(product.getId())
                .stock(stock)
                .build();
        return inventoryRepository.save(inventory);
    }

    private Coupon createCoupon(BigDecimal discount, int maxQuantity) {
        return Coupon.builder()
                .couponName("통합테스트쿠폰")
                .discountType(CouponDiscountType.FIXED_AMOUNT)
                .discountValue(discount)
                .maxQuantity(maxQuantity)
                .issuedQuantity(0)
                .validStartDate(LocalDate.now().minusDays(1))
                .validEndDate(LocalDate.now().plusDays(5))
                .couponType(CouponType.LIMITED)
                .build();
    }
}
