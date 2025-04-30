package com.hhplusecommerce.interfaces.order;

import com.hhplusecommerce.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;

    /**
     * 매 1분마다 실행되는 주문 만료 처리 스케줄러.
     * 생성된 지 1~2분이 지난 대기 주문들을 만료 상태로 변경합니다.
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void expireUnpaidOrders() {
        try {
            log.info("[Scheduler] 결제 대기 주문 만료 처리 시작");
            orderService.expireOverdueOrders();
        } catch (Exception e) {
            log.error("[Scheduler] 결제 대기 주문 만료 처리 실패", e);
        }
    }
}
