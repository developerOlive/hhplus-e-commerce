package com.hhplusecommerce.integration.payment;

import com.hhplusecommerce.IntegrationTestSupport;
import com.hhplusecommerce.application.payment.PaymentFacade;
import com.hhplusecommerce.application.payment.PaymentResult;
import com.hhplusecommerce.domain.balance.BalanceRepository;
import com.hhplusecommerce.domain.balance.UserBalance;
import com.hhplusecommerce.domain.coupon.*;
import com.hhplusecommerce.domain.order.*;
import com.hhplusecommerce.domain.payment.PaymentCommand;
import com.hhplusecommerce.domain.payment.PaymentStatus;
import com.hhplusecommerce.domain.popularProduct.ProductSalesStatsService;
import com.hhplusecommerce.domain.product.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class PaymentFacadeIntegrationTest extends IntegrationTestSupport {

    private static final Long USER_ID = 1L;
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(10000);
    private static final int QUANTITY = 1;
    private static final BigDecimal BALANCE_AMOUNT = BigDecimal.valueOf(50000);
    private static final BigDecimal DISCOUNT_AMOUNT = BigDecimal.valueOf(3000);
    private static final int STOCK_QUANTITY = 5;

    @Autowired private PaymentFacade paymentFacade;
    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductInventoryRepository inventoryRepository;
    @Autowired private BalanceRepository balanceRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private CouponHistoryRepository couponHistoryRepository;
    @Autowired private ProductSalesStatsService productSalesStatsService;

    private Product product;
    private Coupon coupon;
    private Long orderId;

    @BeforeEach
    void setUp() {
        product = productRepository.save(createProduct("에센스", "스킨케어", ITEM_PRICE));
        inventoryRepository.save(createInventory(product, STOCK_QUANTITY));

        coupon = couponRepository.save(createCoupon(DISCOUNT_AMOUNT, 10));
        couponHistoryRepository.save(CouponHistory.issue(USER_ID, coupon));

        balanceRepository.save(UserBalance.create(USER_ID, BALANCE_AMOUNT));

        OrderItemCommand orderItem = new OrderItemCommand(product.getId(), QUANTITY, ITEM_PRICE);
        OrderCommand orderCommand = new OrderCommand(USER_ID, couponHistoryRepository.findCouponsByUserIdAndStatus(USER_ID, CouponUsageStatus.AVAILABLE).get(0).getId(), List.of(orderItem));
        orderId = orderService.createOrder(orderCommand, DISCOUNT_AMOUNT);
    }

    @Nested
    class 결제_성공 {

        @Test
        void 결제를_정상적으로_완료하면_주문은_COMPLETE_상태가_된다() {
            PaymentResult result = paymentFacade.completePayment(new PaymentCommand(orderId, null));

            Order completedOrder = orderService.getOrder(orderId);

            assertAll(
                    () -> assertThat(result.paymentId()).isNotNull(),
                    () -> assertThat(result.paidAmount()).isEqualTo(ITEM_PRICE.subtract(DISCOUNT_AMOUNT)),
                    () -> assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS),
                    () -> assertThat(completedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED)
            );
        }
    }

    @Nested
    class 결제_실패 {

        @Test
        void 잔액이_부족하면_예외가_발생한다() {
            balanceRepository.findByUserId(USER_ID).ifPresent(balance -> balance.deduct(BALANCE_AMOUNT));

            assertThatThrownBy(() -> paymentFacade.completePayment(new PaymentCommand(orderId, null)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());
        }

        @Test
        void 재고가_부족하면_예외가_발생한다() {
            inventoryRepository.findInventoryByProductId(product.getId()).ifPresent(i -> i.decrease(STOCK_QUANTITY));

            assertThatThrownBy(() -> paymentFacade.completePayment(new PaymentCommand(orderId, null)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_STOCK.getMessage());
        }

        @Test
        void 쿠폰이_이미_사용된_경우_예외가_발생한다() {
            CouponHistory history = couponHistoryRepository.findCouponsByUserIdAndStatus(USER_ID, CouponUsageStatus.AVAILABLE).get(0);
            history.use();

            assertThatThrownBy(() -> paymentFacade.completePayment(new PaymentCommand(orderId, null)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_ALREADY_USED.getMessage());
        }
    }

    @Nested
    class 결제_중간에_실패_시_주문_상태는_PAYMENT_WAIT_으로_남는다 {

        @Test
        void 잔액이_부족하면_주문상태는_PAYMENT_WAIT으로_남는다() {
            // 잔액 부족 상태
            balanceRepository.findByUserId(USER_ID).ifPresent(balance -> balance.deduct(BALANCE_AMOUNT));

            // 결제 시도
            assertThatThrownBy(() -> paymentFacade.completePayment(new PaymentCommand(orderId, null)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());

            // 주문 상태 확인
            Order order = orderService.getOrder(orderId);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);  // 주문 상태는 PAYMENT_WAIT
        }

        @Test
        void 재고가_부족하면_주문상태는_PAYMENT_WAIT으로_남는다() {
            // 재고 부족 상태
            inventoryRepository.findInventoryByProductId(product.getId()).ifPresent(i -> i.decrease(STOCK_QUANTITY));

            // 결제 시도
            assertThatThrownBy(() -> paymentFacade.completePayment(new PaymentCommand(orderId, null)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_STOCK.getMessage());

            // 주문 상태 확인
            Order order = orderService.getOrder(orderId);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);  // 주문 상태는 PAYMENT_WAIT
        }

        @Test
        void 쿠폰이_이미_사용된_경우_주문상태는_PAYMENT_WAIT으로_남는다() {
            CouponHistory history = couponHistoryRepository.findCouponsByUserIdAndStatus(USER_ID, CouponUsageStatus.AVAILABLE).get(0);
            history.use();

            // 결제 시도
            assertThatThrownBy(() -> paymentFacade.completePayment(new PaymentCommand(orderId, null)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_ALREADY_USED.getMessage());

            // 주문 상태 확인
            Order order = orderService.getOrder(orderId);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);  // 주문 상태는 PAYMENT_WAIT
        }
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
                .product(product)
                .stock(stock)
                .build();
        product.setInventory(inventory);
        return inventory;
    }

    private Coupon createCoupon(BigDecimal discount, int maxQuantity) {
        return Coupon.builder()
                .couponName("결제통합쿠폰")
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
