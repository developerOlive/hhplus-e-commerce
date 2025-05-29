package com.hhplusecommerce.interfaces.event.dataPlatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderCommand;
import com.hhplusecommerce.domain.order.OrderItemCommand;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.service.OutboxEventService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDataEventListenerTest {
    private static final String TEST_TOPIC_NAME = "order-completed-topic";
    private static final Long TEST_ORDER_ID = 1L;
    private static final Long TEST_USER_ID = 100L;
    private static final Long TEST_PRODUCT_ID = 10L;
    private static final int TEST_QUANTITY = 2;
    private static final BigDecimal TEST_PRICE = BigDecimal.valueOf(10000);
    private static final String TEST_CATEGORY = "ELECTRONICS";
    private static final String MOCKED_EVENT_PAYLOAD = "{\"key\":\"value\"}";

    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private OutboxEventService outboxEventService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SlackNotifier slackNotifier;

    @InjectMocks
    private OrderDataEventListener listener;

    private OrderEvent.Completed testOrderCompletedEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "topicName", TEST_TOPIC_NAME);

        OrderItemCommand itemCommand = new OrderItemCommand(
                TEST_PRODUCT_ID, TEST_QUANTITY, TEST_PRICE, TEST_CATEGORY);

        OrderCommand command = new OrderCommand(
                TEST_USER_ID,
                null,
                List.of(itemCommand)
        );

        Order order = Order.create(command);
        ReflectionTestUtils.setField(order, "id", TEST_ORDER_ID);

        testOrderCompletedEvent = new OrderEvent.Completed(order.getOrderItems());
    }

    @Nested
    class Outbox_이벤트_저장_시나리오 {

        @Test
        void 주문완료이벤트_수신시_아웃박스에_이벤트가_성공적으로_저장된다() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any(OrderEvent.Completed.class))).thenReturn(MOCKED_EVENT_PAYLOAD);
            ArgumentCaptor<OutboxEvent> outboxEventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);

            // When
            listener.saveOutbox(testOrderCompletedEvent);

            // Then
            verify(outboxEventService, times(1)).save(outboxEventCaptor.capture());
            OutboxEvent capturedEvent = outboxEventCaptor.getValue();

            assertThat(capturedEvent.getAggregateType()).isEqualTo(testOrderCompletedEvent.aggregateType());
            assertThat(capturedEvent.getAggregateId()).isEqualTo(String.valueOf(TEST_ORDER_ID));
            assertThat(capturedEvent.getTopicName()).isEqualTo(TEST_TOPIC_NAME);
            assertThat(capturedEvent.getPayload()).isEqualTo(MOCKED_EVENT_PAYLOAD);
            assertThat(capturedEvent.getStatus()).isEqualTo(OutboxStatus.WAITING_FOR_PUBLISH);
            assertThat(capturedEvent.getRetryCount()).isEqualTo(0);
            assertThat(capturedEvent.getCreatedAt()).isNotNull();
            assertThat(capturedEvent.getNextRetryAt()).isNotNull();

            verify(objectMapper, times(1)).writeValueAsString(testOrderCompletedEvent);
        }

        @Test
        void 아웃박스_저장중_예외발생시_로그만_남기고_예외가_전파되지_않는다() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any(OrderEvent.Completed.class))).thenReturn(MOCKED_EVENT_PAYLOAD);
            doThrow(new RuntimeException("DB 저장 실패")).when(outboxEventService).save(any(OutboxEvent.class));

            // When
            listener.saveOutbox(testOrderCompletedEvent);

            // Then
            verify(outboxEventService, times(1)).save(any(OutboxEvent.class));
        }
    }

    @Nested
    class Kafka_비동기_전송_시나리오 {

        @Test
        void 이벤트_수신시_Kafka로_메시지가_성공적으로_발행된다() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any(OrderEvent.Completed.class))).thenReturn(MOCKED_EVENT_PAYLOAD);

            // When
            listener.sendToKafka(testOrderCompletedEvent);

            // Then
            verify(objectMapper, times(1)).writeValueAsString(testOrderCompletedEvent);
            // KafkaProducer.send의 key 인자가 String이므로 Long을 String으로 변환합니다.
            verify(kafkaProducer, times(1)).send(
                    eq(TEST_TOPIC_NAME),
                    eq(String.valueOf(TEST_ORDER_ID)),
                    eq(MOCKED_EVENT_PAYLOAD)
            );
        }

        @Test
        void Kafka_메시지_발행중_예외발생시_로그만_남기고_예외가_전파되지않는다() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any(OrderEvent.Completed.class))).thenReturn(MOCKED_EVENT_PAYLOAD);
            doThrow(new RuntimeException("Kafka 전송 실패")).when(kafkaProducer).send(anyString(), anyString(), anyString());

            // When
            listener.sendToKafka(testOrderCompletedEvent);

            // Then
            verify(kafkaProducer, times(1)).send(
                    eq(TEST_TOPIC_NAME),
                    eq(String.valueOf(TEST_ORDER_ID)),
                    eq(MOCKED_EVENT_PAYLOAD)
            );
        }

        @Test
        void Kafka_JSON_직렬화_실패시_예외가_발생한다() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any(OrderEvent.Completed.class)))
                    .thenThrow(new RuntimeException("JSON 직렬화 실패"));

            // When & Then
            assertThatThrownBy(() -> listener.sendToKafka(testOrderCompletedEvent))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("JSON 직렬화 실패");

            verify(objectMapper, times(1)).writeValueAsString(testOrderCompletedEvent);
            verify(kafkaProducer, never()).send(anyString(), anyString(), anyString());
        }
    }
}
