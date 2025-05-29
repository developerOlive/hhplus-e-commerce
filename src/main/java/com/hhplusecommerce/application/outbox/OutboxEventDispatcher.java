package com.hhplusecommerce.application.outbox;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.port.OutboxEventPort;
import com.hhplusecommerce.infrastructure.kafka.producer.KafkaProducer;
import com.hhplusecommerce.support.notification.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka로 발송되지 않은 Outbox 이벤트를 조회하여 전송하고 상태를 갱신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventDispatcher {

    private final OutboxEventPort outboxEventPort;
    private final KafkaProducer kafkaProducer;
    private final SlackNotifier slackNotifier;

    private static final int BATCH_SIZE = 100;

    public void dispatchPendingEvents() {
        List<OutboxEvent> events = outboxEventPort.findReadyToDispatch(BATCH_SIZE, LocalDateTime.now());
        for (OutboxEvent event : events) {
            try {
                kafkaProducer.send(event.getTopicName(), event.getAggregateId(), event.getPayload());
                outboxEventPort.update(event.markSuccess());
            } catch (Exception e) {
                if (event.getStatus().canRetry()) {
                    outboxEventPort.update(event.markFailed());
                } else {
                    log.error("[Outbox] 재시도 불가능 이벤트 - topicName={}, aggregateId={}, error={}", event.getTopicName(), event.getAggregateId(), e.getMessage(), e);
                    slackNotifier.send(String.format(
                            "[Outbox ERROR] 재시도 불가능 이벤트 - topicName=%s, aggregateId=%s",
                            event.getTopicName(), event.getAggregateId()
                    ));
                }
            }
        }
    }
}
