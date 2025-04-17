package com.hhplusecommerce.application.order;

import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.CouponService;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderCommand;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.order.OrderStatus;
import com.hhplusecommerce.domain.product.ProductInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


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

        // 재고 확인
        inventoryService.validateAllProductStocks(command.orderItems());

        // 총액 계산
        Order order = Order.create(command);
        BigDecimal totalAmount = order.getTotalAmount();

        // 할인 금액 계산
        BigDecimal discountAmount = couponService.calculateDiscount(userId, couponIssueId, totalAmount);

        // 최종 결제 금액 계산
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        // 잔액 확인
        balanceService.validateEnough(userId, finalAmount);

        // 할인 금액 적용 및 저장
        Long orderId = orderService.createOrder(command, discountAmount);

        // 결과 반환
        return new OrderResult(orderId, totalAmount, finalAmount, OrderStatus.PAYMENT_WAIT);
    }
}
