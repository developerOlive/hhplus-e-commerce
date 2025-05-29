package com.hhplusecommerce.domain.outbox;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.type.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    private static final String AGGREGATE_TYPE = "Order";
    private static final String AGGREGATE_ID = "123";
    private static final String TOPIC_NAME = "order-topic";
    private static final String PAYLOAD = "{}";

    private OutboxEvent event;

    @BeforeEach
    void setUp() {
        event = OutboxEvent.builder()
                .id(1L)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(AGGREGATE_ID)
                .topicName(TOPIC_NAME)
                .payload(PAYLOAD)
                .status(OutboxStatus.WAITING_FOR_PUBLISH)
                .retryCount(0)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .nextRetryAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class 재시도_실패_처리_테스트 {

        @Test
        void retryCount가_1증가하고_nextRetryAt이_뒤로_밀리며_status가_FAILED로_변경된다() {
            OutboxEvent failedEvent = event.markFailed();

            assertThat(failedEvent.getRetryCount()).isEqualTo(event.getRetryCount() + 1);
            assertThat(failedEvent.getNextRetryAt()).isAfter(event.getNextRetryAt());
            assertThat(failedEvent.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(failedEvent.getAggregateId()).isEqualTo(event.getAggregateId());
            assertThat(failedEvent.getPayload()).isEqualTo(event.getPayload());
        }
    }

    @Nested
    class 성공_처리_테스트 {

        @Test
        void status가_SUCCESS로_변경되고_retryCount는_변하지_않는다() {
            OutboxEvent successEvent = event.markSuccess();

            assertThat(successEvent.getStatus()).isEqualTo(OutboxStatus.SUCCESS);
            assertThat(successEvent.getRetryCount()).isEqualTo(event.getRetryCount());
        }
    }
}
