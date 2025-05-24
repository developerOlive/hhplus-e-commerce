package com.hhplusecommerce.support.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSlackNotifier implements SlackNotifier {

    @Override
    public void send(String message) {
        log.info("[MockSlackNotifier] 슬랙 메시지 전송 시뮬레이션: {}", message);
    }
}
