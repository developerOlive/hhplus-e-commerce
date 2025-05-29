package com.hhplusecommerce.integration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class KafkaTestConsumer {

    private final AtomicLong messageCount = new AtomicLong(0);

    @KafkaListener(topics = "test-topic", groupId = "${test.kafka.groupId}")
    public void listen(String message) {
        log.info("Received message: {}", message);
        messageCount.incrementAndGet();
    }

    public long getMessageCount() {
        return messageCount.get();
    }

    public void reset() {
        messageCount.set(0);
    }
}
