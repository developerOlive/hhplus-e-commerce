package com.hhplusecommerce.integration.event;

import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import com.hhplusecommerce.interfaces.event.dataPlatform.OrderDataEventListener;
import com.hhplusecommerce.interfaces.event.ranking.OrderRankingEventListener;
import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.support.trace.EventTraceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderEventIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventTraceManager eventTraceManager;

    @SpyBean
    private OrderRankingEventListener rankingListener;

    @SpyBean
    private OrderDataEventListener dataListener;

    @BeforeEach
    void clearSpies() {
        clearInvocations(rankingListener, dataListener);
    }

    private OrderItems createValidOrderItems(Long orderId) {
        Order mockOrder = mock(Order.class);
        when(mockOrder.getId()).thenReturn(orderId);
        OrderItem item = new OrderItem(mockOrder, 1L, 2, BigDecimal.valueOf(1000), "electronic");
        return new OrderItems(List.of(item));
    }

    @Nested
    class 트랜잭션_정상_커밋_상황 {

        private static final Long ORDER_ID = 101L;

        @Test
        void 트랜잭션_내에서_커밋되면_일반리스너와_AFTER_COMMIT_리스너가_1번씩_실행된다() {
            // given
            OrderItems orderItems = createValidOrderItems(ORDER_ID);

            // when
            transactionTemplate.executeWithoutResult(status -> {
                eventTraceManager.publish(new OrderEvent.Completed(orderItems));
            });

            // then
            verify(rankingListener, timeout(3000)).handle(any(OrderEvent.Completed.class));
            verify(dataListener, timeout(3000)).bufferEvent(any(OrderEvent.Completed.class));
        }
    }

    @Nested
    class 트랜잭션_롤백_상황 {

        private static final Long ORDER_ID = 102L;

        @Test
        void 트랜잭션_내에서_롤백되면_트랜잭션리스너는_실행되지_않는다() {
            // given
            OrderItems orderItems = createValidOrderItems(ORDER_ID);

            // when & then
            assertThatThrownBy(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        eventTraceManager.publish(new OrderEvent.Completed(orderItems));
                        throw new RuntimeException("강제 롤백");
                    })
            ).isInstanceOf(RuntimeException.class);

            // then
            verify(rankingListener, never()).handle(any(OrderEvent.Completed.class));
            verify(dataListener, never()).bufferEvent(any(OrderEvent.Completed.class));
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
            eventTraceManager.publish(new OrderEvent.Completed(orderItems));

            // then
            verify(rankingListener, never()).handle(any(OrderEvent.Completed.class));
            verify(dataListener, never()).bufferEvent(any(OrderEvent.Completed.class));
        }
    }
}
