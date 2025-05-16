package com.hhplusecommerce.application.payment;

import com.hhplusecommerce.domain.balance.BalanceCommand;
import com.hhplusecommerce.domain.balance.BalanceService;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderService;
import com.hhplusecommerce.domain.payment.Payment;
import com.hhplusecommerce.domain.payment.PaymentCommand;
import com.hhplusecommerce.domain.payment.PaymentService;
import com.hhplusecommerce.domain.popularProduct.service.PopularProductRankingService;
import com.hhplusecommerce.domain.popularProduct.service.ProductSalesStatsService;
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
    private final PopularProductRankingService popularProductRankingService;

    @Transactional
    public PaymentResult completePayment(PaymentCommand command) {
        // 주문 조회
        Order order = orderService.getOrder(command.orderId());
        Long userId = order.getUserId();
        BigDecimal finalAmount = order.getFinalAmount();

        // 잔액 차감
        balanceService.deductBalance(new BalanceCommand(userId, finalAmount));

        // 주문 항목 조회 및 재고 차감
        List<OrderItem> orderItems = order.getOrderItems();
        inventoryService.decreaseStocks(orderItems);

        // 쿠폰 사용 처리
        if (order.hasCoupon()) {
            couponService.useCoupon(userId, order.getCouponIssueId());
        }

        // 주문 상태 완료 처리
        order.complete();

        // 결제 정보 저장
        Payment payment = paymentService.completePayment(order.getId(), finalAmount);

        // 판매 통계 반영 (RDB, Redis) - 추후 비동기 처리 예정 (결제와 통계 분리)
        productSalesStatsService.recordSales(orderItems, LocalDate.now());
        popularProductRankingService.recordSales(orderItems);

        return new PaymentResult(payment.getId(), payment.getPaidAmount(), payment.getPaymentStatus()
        );
    }
}
