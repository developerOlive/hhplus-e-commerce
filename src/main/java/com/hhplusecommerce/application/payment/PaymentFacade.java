package com.hhplusecommerce.application.payment;

import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.CouponService;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.payment.Payment;
import com.hhplusecommerce.domain.payment.PaymentCommand;
import com.hhplusecommerce.domain.payment.PaymentService;
import com.hhplusecommerce.domain.popularProduct.ProductSalesStatsService;
import com.hhplusecommerce.domain.product.ProductInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentFacade {

    private final BalanceService balanceService;
    private final ProductInventoryService inventoryService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductSalesStatsService productSalesStatsService;

    @Transactional
    public PaymentResult completePayment(PaymentCommand command) {
        // 주문 조회
        Order order = orderService.getOrder(command.orderId());
        Long userId = order.getUserId();
        BigDecimal finalAmount = order.getFinalAmount();

        // 잔액 차감
        balanceService.deductBalance(new BalanceCommand(userId, finalAmount));

        // 주문 항목 조회 및 재고 차감
        List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
        inventoryService.decreaseStocks(orderItems);

        // 쿠폰 사용 처리
        if (order.hasCoupon()) {
            couponService.useCoupon(userId, order.getCouponIssueId());
        }

        // 주문 상태 완료 처리
        order.complete();

        // 결제 정보 저장
        Payment payment = paymentService.completePayment(order.getId(), finalAmount);

        // 판매 통계 반영
        try {
            productSalesStatsService.recordSales(orderItems, LocalDate.now());
        } catch (Exception e) {
            // 판매 통계 기록 실패 시 로그에 상품, 수량, 날짜 포함하여 기록 (복구용)
            for (OrderItem item : orderItems) {
                log.error("판매 통계 기록에 실패했습니다. 상품 ID: {}, 수량: {}, 날짜: {}, 오류 메시지: {}",
                        item.getProductId(),
                        item.getQuantity(),
                        LocalDate.now(),
                        e.getMessage());
            }
        }

        return new PaymentResult(
                payment.getId(),
                payment.getPaidAmount(),
                payment.getPaymentStatus()
        );
    }
}
