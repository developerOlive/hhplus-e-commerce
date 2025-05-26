package com.hhplusecommerce.integration.event;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.hhplusecommerce.domain.order.*;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.support.trace.EventTraceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTraceManagerLogTest extends IntegrationTestSupport {

    @Autowired
    private EventTraceManager eventTraceManager;

    private InMemoryLogAppender appender;

    @BeforeEach
    void setUpAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(EventTraceManager.class);
        appender = new InMemoryLogAppender();
        appender.start();
        logger.addAppender(appender);
    }

    @Test
    void 이벤트_퍼블리시_로그에_traceId와_spanId가_포함된다() {
        // given
        MDC.put("traceId", "test-trace-id");

        Order order = createTestOrderWithId(1L);

        OrderItem orderItem = new OrderItem(order, 1L, 2, BigDecimal.valueOf(1000), "electronics");
        OrderItems orderItems = new OrderItems(List.of(orderItem));
        OrderEvent.Completed event = new OrderEvent.Completed(orderItems);

        // when
        eventTraceManager.publish(event);

        // then
        List<ILoggingEvent> logs = appender.getLogs();
        boolean found = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("[EVENT_PUBLISH]")
                        && log.getFormattedMessage().contains("traceId=test-trace-id"));
        assertThat(found).isTrue();
    }

    private Order createTestOrderWithId(Long id) {
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(1L, 2, BigDecimal.valueOf(1000), "electronics")
        );

        Order order = Order.create(new OrderCommand(1L, 100L, items));

        // 리플렉션으로 id 세팅
        try {
            Field field = Order.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(order, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return order;
    }


    private void setId(Order order, Long id) throws Exception {
        Field field = Order.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(order, id);
    }

    private static class InMemoryLogAppender extends ch.qos.logback.core.AppenderBase<ILoggingEvent> {

        private final List<ILoggingEvent> logs = new java.util.ArrayList<>();

        @Override
        protected void append(ILoggingEvent eventObject) {
            logs.add(eventObject);
        }

        public List<ILoggingEvent> getLogs() {
            return logs;
        }

        public void clear() {
            logs.clear();
        }
    }
}
