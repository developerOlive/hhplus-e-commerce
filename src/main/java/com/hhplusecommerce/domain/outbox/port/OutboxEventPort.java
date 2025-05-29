package com.hhplusecommerce.domain.outbox.port;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OutBox 이벤트 저장, 조회, 상태 변경 기능 인터페이스
 */
public interface OutboxEventPort {
    void save(OutboxEvent event);
    List<OutboxEvent> findReadyToDispatch(int batchSize, LocalDateTime now);
    void update(OutboxEvent event);
    Optional<OutboxEvent> findByAggregateIdAndTopic(String aggregateId, String topicName);

}
