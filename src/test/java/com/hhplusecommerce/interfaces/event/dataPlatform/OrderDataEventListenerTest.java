package com.hhplusecommerce.interfaces.event.dataPlatform;

import com.hhplusecommerce.application.external.DataPlatformExporter;
import com.hhplusecommerce.domain.order.Order;
import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.order.OrderItems;
import com.hhplusecommerce.domain.order.event.OrderEvent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDataEventListenerTest {
    private static final Long PRODUCT_ID = 100L;
    private static final int QUANTITY = 2;
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(5000);
    private static final String CATEGORY = "book";

    @Mock
    private DataPlatformExporter dataPlatformExporter;

    @InjectMocks
    private OrderDataEventListener listener;

    private OrderItems createOrderItems() {
        Order mockOrder = mock(Order.class);
        OrderItem item = new OrderItem(mockOrder, PRODUCT_ID, QUANTITY, UNIT_PRICE, CATEGORY);

        return new OrderItems(List.of(item));
    }

    @Nested
    class 주문완료_이벤트를_수신했을_때 {

        @Test
        void 외부플랫폼으로_주문정보전송이_수행된다() {
            // given
            OrderItems orderItems = createOrderItems();
            OrderEvent.Completed event = new OrderEvent.Completed(orderItems);

            // when
            listener.handle(event);

            // then
            verify(dataPlatformExporter, timeout(1000)).sendOrder(orderItems);
        }
    }
}
