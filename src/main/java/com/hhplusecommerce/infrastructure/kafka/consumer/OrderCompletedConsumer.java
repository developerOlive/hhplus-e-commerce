package com.hhplusecommerce.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.application.external.DataPlatformExporter;
import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompletedConsumer {

    private final ObjectMapper objectMapper;
    private final DataPlatformExporter dataPlatformExporter;
    private final SlackNotifier slackNotifier;

    @KafkaListener(
            topics = "${custom.kafka.topic.order-completed}",
            groupId = "${custom.kafka.consumer.group-id.order-data-platform}"
    )
    public void listen(ConsumerRecord<String, String> record) {
        try {
            OrderItems orderItems = objectMapper.readValue(record.value(), OrderItems.class);
            dataPlatformExporter.sendOrder(orderItems);

            log.info("[Kafka] 주문 이벤트 수신 및 처리 완료 - orderId={}", orderItems.getOrderId());
        } catch (Exception e) {
            log.error("[Kafka] 주문 이벤트 처리 실패: {}", record.value(), e);
            slackNotifier.send("[Kafka ERROR] 주문 이벤트 처리 실패 - payload=" + record.value());
        }
    }
}
