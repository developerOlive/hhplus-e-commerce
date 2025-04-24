package com.hhplusecommerce.application.order;

import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.CouponService;
import com.hhplusecommerce.domain.order.OrderCommand;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.order.OrderStatus;
import com.hhplusecommerce.domain.product.ProductInventoryService;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long COUPON_ISSUE_ID = 99L;
    private static final Long ORDER_ID = 123L;

    private static final Long PRODUCT_ID = 10L;
    private static final int QUANTITY = 2;
    private static final BigDecimal ITEM_PRICE = new BigDecimal("5000");
    private static final BigDecimal TOTAL_AMOUNT = ITEM_PRICE.multiply(BigDecimal.valueOf(QUANTITY));
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("3000");
    private static final BigDecimal FINAL_AMOUNT = TOTAL_AMOUNT.subtract(DISCOUNT_AMOUNT);

    private static final List<OrderItemCommand> ITEMS = List.of(
            new OrderItemCommand(PRODUCT_ID, QUANTITY, ITEM_PRICE)
    );

    @InjectMocks
    private OrderFacade orderFacade;

    @Mock(lenient = true)
    private ProductInventoryService inventoryService;
    @Mock private BalanceService balanceService;
    @Mock private CouponService couponService;
    @Mock private OrderService orderService;

    @Nested
    class 주문_성공 {

        @Test
        void 주문을_정상적으로_생성한다() {
            // given
            OrderCommand command = new OrderCommand(USER_ID, COUPON_ISSUE_ID, ITEMS);

            doNothing().when(inventoryService).validateAllProductStocks(ITEMS);
            when(couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT)).thenReturn(DISCOUNT_AMOUNT);
            doNothing().when(balanceService).validateEnough(USER_ID, FINAL_AMOUNT);
            when(orderService.createOrder(command, DISCOUNT_AMOUNT)).thenReturn(ORDER_ID);

            // when
            OrderResult result = orderFacade.placeOrder(command);

            // then
            assertThat(result.orderId()).isEqualTo(ORDER_ID);
            assertThat(result.totalAmount()).isEqualTo(TOTAL_AMOUNT);
            assertThat(result.finalAmount()).isEqualTo(FINAL_AMOUNT);
            assertThat(result.orderStatus()).isEqualTo(OrderStatus.PAYMENT_WAIT);
        }
    }

    @Nested
    class 주문_실패 {

        @Test
        void 재고가_부족하면_예외가_발생한다() {
            // given: 상품 재고가 부족한 상태 (차감 시도 시 예외 발생하도록 설정)
            OrderCommand command = new OrderCommand(USER_ID, COUPON_ISSUE_ID, ITEMS);
            doThrow(new CustomException(ErrorType.INSUFFICIENT_STOCK))
                    .when(inventoryService).validateAllProductStocks(ITEMS);

            // when & then: 주문 시 예외가 발생하고 이후 로직은 실행되지 않아야 함
            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_STOCK.getMessage());

            verifyNoInteractions(couponService, balanceService, orderService);
        }

        @Test
        void 유효하지_않은_쿠폰이면_예외가_발생한다() {
            // given: 쿠폰 조회 단계에서 예외 발생하도록 설정
            OrderCommand command = new OrderCommand(USER_ID, COUPON_ISSUE_ID, ITEMS);
            doNothing().when(inventoryService).decreaseStock(PRODUCT_ID, QUANTITY);
            when(couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT))
                    .thenThrow(new CustomException(ErrorType.COUPON_NOT_FOUND));

            // when & then: 예외 발생하고 잔액 검증 및 주문 저장 로직은 실행되지 않아야 함
            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.COUPON_NOT_FOUND.getMessage());

            verifyNoInteractions(balanceService, orderService);
        }

        @Test
        void 잔액이_부족하면_예외가_발생한다() {
            // given: 잔액 부족 예외 상황 설정
            OrderCommand command = new OrderCommand(USER_ID, COUPON_ISSUE_ID, ITEMS);
            doNothing().when(inventoryService).decreaseStock(PRODUCT_ID, QUANTITY);
            when(couponService.calculateDiscount(USER_ID, COUPON_ISSUE_ID, TOTAL_AMOUNT))
                    .thenReturn(DISCOUNT_AMOUNT);
            doThrow(new CustomException(ErrorType.INSUFFICIENT_BALANCE))
                    .when(balanceService).validateEnough(USER_ID, FINAL_AMOUNT);

            // when & then: 예외 발생하고 주문은 생성되지 않아야 함
            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorType.INSUFFICIENT_BALANCE.getMessage());

            verifyNoInteractions(orderService);
        }
    }
}
