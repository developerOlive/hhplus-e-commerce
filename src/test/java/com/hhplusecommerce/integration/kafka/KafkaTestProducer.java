package com.hhplusecommerce.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "test-topic";

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        log.info("Sent message='{}' to topic='{}'", message, TOPIC);
    }
}
