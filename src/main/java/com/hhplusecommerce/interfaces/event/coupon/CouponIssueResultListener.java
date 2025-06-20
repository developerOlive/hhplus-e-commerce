package com.hhplusecommerce.interfaces.event.coupon;

import com.hhplusecommerce.application.coupon.event.CouponIssueCompletedEvent;
import com.hhplusecommerce.support.notification.SlackNotifier;
import com.hhplusecommerce.support.notification.AppPushNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueResultListener {

    private final SlackNotifier slackNotifier;
    private final AppPushNotifier appPushNotifier;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponIssueCompletedEvent(CouponIssueCompletedEvent event) {
        try {
            switch (event.result()) {
                case SUCCESS, OUT_OF_STOCK, ALREADY_ISSUED -> {
                    appPushNotifier.sendPush(event.userId(), event.message());
                }
                case FAILED_SYSTEM -> {
                    slackNotifier.send(String.format(
                            "[쿠폰 발급 오류] 시스템 오류 발생 - 사용자ID: %d, 쿠폰ID: %d, 메시지: %s",
                            event.userId(), event.couponId(), event.message()
                    ));
                }
            }
        } catch (Exception e) {
            log.error("CouponIssueCompletedEvent 처리 중 예외 발생: userId={}, couponId={}, result={}",
                    event.userId(), event.couponId(), event.result(), e);
            try {
                slackNotifier.send(String.format(
                        "[재시도 전송] 쿠폰 발급 처리 중 예외 발생 - 사용자ID: %d, 쿠폰ID: %d, 메시지: %s",
                        event.userId(), event.couponId(), event.message()
                ));
            } catch (Exception slackEx) {
                log.warn("슬랙 오류 채널 전송 실패", slackEx);
            }
        }
    }
}
