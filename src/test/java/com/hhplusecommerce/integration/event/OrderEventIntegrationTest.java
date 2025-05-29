package com.hhplusecommerce.integration.event;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.domain.outbox.service.OutboxEventService;
import com.hhplusecommerce.interfaces.event.dataPlatform.OrderDataEventListener;
import com.hhplusecommerce.interfaces.event.ranking.PopularProductRankingEventListener;
import com.hhplusecommerce.interfaces.event.ranking.ProductSalesStatsEventListener;
import com.hhplusecommerce.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderEventIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private ProductSalesStatsEventListener productSalesStatsListener;

    @MockBean
    private PopularProductRankingEventListener popularProductRankingListener;

    @MockBean
    private OutboxEventService outboxEventService;

    @MockBean
    private OrderDataEventListener dataListener;

    private final AtomicReference<String> capturedTraceId = new AtomicReference<>();
    private final AtomicReference<String> capturedSpanId = new AtomicReference<>();

    @BeforeEach
    void clearMocks() {
        clearInvocations(productSalesStatsListener, popularProductRankingListener, dataListener, outboxEventService);

        // MDC 셋업 (기존 로직 유지)
        doAnswer((Answer<Void>) invocation -> {
            capturedTraceId.set(MDC.get("traceId"));
            capturedSpanId.set(MDC.get("spanId"));
            return null;
        }).when(productSalesStatsListener).handle(any(OrderEvent.Completed.class));

        doAnswer(invocation -> {
            outboxEventService.save(any());
            return null;
        }).when(dataListener).saveOutbox(any(OrderEvent.Completed.class));
    }

    @AfterEach
    void clearMdcCapture() {
        capturedTraceId.set(null);
        capturedSpanId.set(null);
        MDC.clear();
    }

    private OrderItems createValidOrderItems(Long orderId) {
        var mockOrder = mock(com.hhplusecommerce.domain.order.Order.class);
        when(mockOrder.getId()).thenReturn(orderId);
        OrderItem item = new OrderItem(mockOrder, 1L, 2, BigDecimal.valueOf(1000), "electronic");
        return new OrderItems(List.of(item));
    }

    @Nested
    class 트랜잭션_정상_커밋_상황 {

        private static final Long ORDER_ID = 101L;

        @Test
        void 트랜잭션_내에서_커밋되면_각_리스너가_1번씩_실행된다() {
            // given
            OrderItems orderItems = createValidOrderItems(ORDER_ID);

            // when
            transactionTemplate.executeWithoutResult(status -> {
                eventPublisher.publishEvent(new OrderEvent.Completed(orderItems));
            });

            // then
            verify(productSalesStatsListener, timeout(3000)).handle(any(OrderEvent.Completed.class));
            verify(popularProductRankingListener, timeout(3000)).handle(any(OrderEvent.Completed.class));
            verify(outboxEventService, timeout(3000)).save(any());
            verify(dataListener, timeout(3000)).saveOutbox(any(OrderEvent.Completed.class));
        }

        @Test
        void 이벤트_발행시_MDC가_셋업되는지_검증() {
            // given
            String testTraceId = "test-trace-id";
            String testSpanId = "test-span-id";
            MDC.put("traceId", testTraceId);
            MDC.put("spanId", testSpanId);

            OrderItems orderItems = createValidOrderItems(100L);
            OrderEvent.Completed event = new OrderEvent.Completed(orderItems);

            // when
            transactionTemplate.executeWithoutResult(status -> {
                eventPublisher.publishEvent(event);
            });

            // then
            verify(productSalesStatsListener, timeout(3000)).handle(any(OrderEvent.Completed.class));
            assertEquals(testTraceId, capturedTraceId.get());
            assertEquals(testSpanId, capturedSpanId.get());
        }
    }

    @Nested
    class 트랜잭션_롤백_상황 {

        private static final Long ORDER_ID = 102L;

        @Test
        void 트랜잭션_내에서_롤백되면_트랜잭션리스너는_실행되지_않는다() {
            // given
            OrderItems orderItems = createValidOrderItems(ORDER_ID);

            // when
            assertThatThrownBy(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        eventPublisher.publishEvent(new OrderEvent.Completed(orderItems));
                        throw new RuntimeException("강제 롤백");
                    })
            ).isInstanceOf(RuntimeException.class);

            // then
            verify(productSalesStatsListener, never()).handle(any(OrderEvent.Completed.class));
            verify(popularProductRankingListener, never()).handle(any(OrderEvent.Completed.class));
            verify(outboxEventService, never()).save(any());
            verify(dataListener, never()).saveOutbox(any());
        }
    }

    @Nested
    class 트랜잭션_없이_이벤트_발행_상황 {

        private static final Long ORDER_ID = 103L;

        @Test
        void 트랜잭션_없이_이벤트를_발행하면_리스너는_실행되지_않는다() {
            // given
            OrderItems orderItems = createValidOrderItems(ORDER_ID);

            // when
            eventPublisher.publishEvent(new OrderEvent.Completed(orderItems));

            // then
            verify(productSalesStatsListener, never()).handle(any(OrderEvent.Completed.class));
            verify(popularProductRankingListener, never()).handle(any(OrderEvent.Completed.class));
            verify(outboxEventService, never()).save(any());
            verify(dataListener, never()).saveOutbox(any());
        }
    }
}
