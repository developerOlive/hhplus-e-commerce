package com.hhplusecommerce.infrastructure.kafka.producer;

import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 메시지를 발행하고 실패 시 알림을 보내는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SlackNotifier slackNotifier;

    public void send(String topic, String key, String value) {
        try {
            kafkaTemplate.send(topic, key, value);
        } catch (Exception ex) {
            log.error("[Kafka] 발행 실패 - topic={}, key={}, error={}", topic, key, ex.getMessage(), ex);
            slackNotifier.send("[Kafka ERROR] 메시지 발행 실패 - topic=" + topic + ", key=" + key);
        }
    }
}
