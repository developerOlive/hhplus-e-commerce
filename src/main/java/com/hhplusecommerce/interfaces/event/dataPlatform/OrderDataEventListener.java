package com.hhplusecommerce.interfaces.event.dataPlatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.service.OutboxEventService;
import com.hhplusecommerce.infrastructure.kafka.producer.KafkaProducer;
import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.Optional;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderDataEventListener {

    private final KafkaProducer kafkaProducer;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;
    private final SlackNotifier slackNotifier;

    @Value("${custom.kafka.topic.order-completed}")
    private String topicName;

    /**
     * 트랜잭션 커밋 전에 Outbox 테이블에 저장
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutbox(OrderEvent.Completed event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    event.aggregateType(),
                    event.aggregateId(),
                    topicName,
                    payload
            );
            outboxEventService.save(outboxEvent);
        } catch (Exception e) {
            log.error("[Outbox] 저장 실패 - orderId={}", event.aggregateId(), e);
            slackNotifier.send(String.format("[Outbox 저장 실패] orderId=%s, traceId=%s, spanId=%s",
                    event.aggregateId(),
                    event.getTraceId(),
                    event.getSpanId())
            );

        }
    }

    /**
     * 트랜잭션 커밋 이후 Kafka로 비동기 전송
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendToKafka(OrderEvent.Completed event) {
        String payload = toJson(event);
        try {
            kafkaProducer.send(topicName, event.aggregateId(), payload);
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패 - eventType={}, orderId={}, error={}", topicName, event.aggregateId(), e.getMessage(), e);
            slackNotifier.send(String.format("[Kafka 전송 실패] topic=%s, orderId=%s", topicName, event.aggregateId()));

            Optional<OutboxEvent> existingOutboxEvent = outboxEventService.findByAggregateIdAndTopic(
                    event.aggregateId(), topicName
            );

            if (existingOutboxEvent.isPresent()) {
                OutboxEvent failed = existingOutboxEvent.get().markFailed();
                outboxEventService.save(failed);
            } else {
                log.warn("[Kafka] 실패 이벤트 OutboxEvent 찾을 수 없음 - orderId={}", event.aggregateId());
            }
        }
    }

    public String toJson(OrderEvent.Completed event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("[Kafka] JSON 직렬화 실패 - eventType={}, aggregateId={}, error={}",
                    topicName, event.aggregateId(), e.getMessage(), e);
            throw new RuntimeException(
                    String.format("JSON 직렬화 실패: eventType=%s, aggregateId=%s", topicName, event.aggregateId()), e);
        }
    }
}
