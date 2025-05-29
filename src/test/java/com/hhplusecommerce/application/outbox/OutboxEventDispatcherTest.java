package com.hhplusecommerce.application.outbox;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.port.OutboxEventPort;
import com.hhplusecommerce.domain.outbox.type.OutboxStatus;
import com.hhplusecommerce.infrastructure.kafka.producer.KafkaProducer;
import com.hhplusecommerce.support.notification.SlackNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventDispatcherTest {

    private static final String AGGREGATE_TYPE = "Order";
    private static final String AGGREGATE_ID = "123";
    private static final String TOPIC_NAME = "order-topic";
    private static final String PAYLOAD = "{}";

    @Mock
    private OutboxEventPort outboxEventPort;
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private SlackNotifier slackNotifier;
    @InjectMocks
    private OutboxEventDispatcher dispatcher;

    @Nested
    class nextRetryAt_시간이_지났을_때 {
        @Test
        void 이벤트를_전송하고_성공처리한다() {
            // given
            OutboxEvent event = OutboxEvent.builder()
                    .id(1L)
                    .aggregateType(AGGREGATE_TYPE)
                    .aggregateId(AGGREGATE_ID)
                    .topicName(TOPIC_NAME)
                    .payload(PAYLOAD)
                    .status(OutboxStatus.WAITING_FOR_PUBLISH)
                    .retryCount(0)
                    .createdAt(LocalDateTime.now().minusMinutes(10))
                    .nextRetryAt(LocalDateTime.now().minusMinutes(1))
                    .build();

            when(outboxEventPort.findReadyToDispatch(anyInt(), any())).thenReturn(List.of(event));

            // when
            dispatcher.dispatchPendingEvents();

            // then
            verify(kafkaProducer, times(1)).send(eq(TOPIC_NAME), eq(AGGREGATE_ID), eq(PAYLOAD));

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventPort).update(captor.capture());

            OutboxEvent updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo(OutboxStatus.SUCCESS);
        }
    }

    @Nested
    class nextRetryAt_시간이_안_지났을_때 {
        @Test
        void 이벤트를_전송하지_않는다() {
            // given
            OutboxEvent event = OutboxEvent.builder()
                    .id(2L)
                    .aggregateType(AGGREGATE_TYPE)
                    .aggregateId(AGGREGATE_ID)
                    .topicName(TOPIC_NAME)
                    .payload(PAYLOAD)
                    .status(OutboxStatus.FAILED)
                    .retryCount(1)
                    .createdAt(LocalDateTime.now().minusMinutes(10))
                    .nextRetryAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            when(outboxEventPort.findReadyToDispatch(anyInt(), any())).thenReturn(List.of());

            // when
            dispatcher.dispatchPendingEvents();

            // then
            verify(kafkaProducer, never()).send(any(), any(), any());
            verify(outboxEventPort, never()).update(any());
        }
    }

    @Nested
    class 예외가_발생했을_때 {
        @Test
        void 재시도_가능한_이벤트는_실패로_표시한다() {
            // given
            OutboxEvent event = OutboxEvent.builder()
                    .id(3L)
                    .aggregateType(AGGREGATE_TYPE)
                    .aggregateId(AGGREGATE_ID)
                    .topicName(TOPIC_NAME)
                    .payload(PAYLOAD)
                    .status(OutboxStatus.WAITING_FOR_PUBLISH)
                    .retryCount(1)
                    .createdAt(LocalDateTime.now().minusMinutes(10))
                    .nextRetryAt(LocalDateTime.now().minusMinutes(1))
                    .build();

            when(outboxEventPort.findReadyToDispatch(anyInt(), any())).thenReturn(List.of(event));
            doThrow(new RuntimeException("Kafka 전송 실패")).when(kafkaProducer).send(any(), any(), any());

            // when
            dispatcher.dispatchPendingEvents();

            // then
            verify(outboxEventPort).update(argThat(e ->
                    e.getStatus() == OutboxStatus.FAILED &&
                            e.getRetryCount() == event.getRetryCount() + 1
            ));
        }
    }
}
