package com.hhplusecommerce.support.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockAppPushNotifier implements AppPushNotifier {

    @Override
    public void sendPush(Long userId, String message) {
        log.info("[MockAppPushNotifier] 앱 푸시 알림 전송 시뮬레이션: userId={}, message={}", userId, message);
    }
}
