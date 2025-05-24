package com.hhplusecommerce.application.payment;

import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.domain.order.event.OrderEventPublisher;
import com.hhplusecommerce.domain.payment.Payment;
import com.hhplusecommerce.domain.payment.PaymentCommand;
import com.hhplusecommerce.domain.payment.PaymentService;
import com.hhplusecommerce.domain.product.ProductInventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentFacade {

    private final BalanceService balanceService;
    private final ProductInventoryService inventoryService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public PaymentResult completePayment(PaymentCommand command) {
        // 주문 조회
        Order order = orderService.getOrder(command.orderId());
        BigDecimal finalAmount = order.getFinalAmount();

        // 주문 항목 조회 및 재고 차감
        OrderItems orderItems = order.getOrderItems();
        inventoryService.decreaseStocks(orderItems.getItems());

        // 쿠폰 사용 처리
        couponService.useCoupon(order);

        // 잔액 차감
        balanceService.deductBalance(new BalanceCommand(order.getUserId(), finalAmount));

        // 주문 상태 완료 처리
        order.complete();

        // 결제 정보 저장
        Payment payment = paymentService.completePayment(order.getId(), finalAmount);

        // 최종 주문 이벤트 발행
        orderEventPublisher.publish(new OrderEvent.Completed(orderItems));

        return new PaymentResult(payment.getId(), payment.getPaidAmount(), payment.getPaymentStatus());
    }
}
