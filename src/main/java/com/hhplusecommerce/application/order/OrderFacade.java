package com.hhplusecommerce.application.order;

import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.CouponService;
import com.hhplusecommerce.domain.order.OrderCommand;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.order.OrderStatus;
import com.hhplusecommerce.domain.product.ProductInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.hhplusecommerce.domain.order.OrderAmountCalculator.calculateTotalAmount;

@Service
@RequiredArgsConstructor
public class OrderFacade {
    private final ProductInventoryService inventoryService;
    private final BalanceService balanceService;
    private final CouponService couponService;
    private final OrderService orderService;

    @Transactional
    public OrderResult placeOrder(OrderCommand command) {
        Long userId = command.userId();
        Long couponIssueId = command.couponIssueId();
        List<OrderItemCommand> items = command.orderItems();

        // 재고 확인
        inventoryService.validateAllProductStocks(items);

        // 총액 계산
        BigDecimal totalAmount = calculateTotalAmount(items);

        // 할인 계산
        BigDecimal discountAmount = couponService.calculateDiscount(userId, couponIssueId, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        // 잔액 확인
        balanceService.validateEnough(userId, finalAmount);

        // 주문 생성
        Long orderId = orderService.createOrder(command, totalAmount, finalAmount);
        return new OrderResult(orderId, totalAmount, finalAmount, OrderStatus.PAYMENT_WAIT);
    }

}
