package com.hhplusecommerce.infrastructure.outbox;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.port.OutboxEventPort;
import com.hhplusecommerce.domain.outbox.type.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventPort {

    private final OutboxEventJpaRepository jpaRepository;

    @Override
    public void save(OutboxEvent event) {
        jpaRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findReadyToDispatch(int batchSize, LocalDateTime now) {
        Pageable pageable = PageRequest.of(0, batchSize);
        List<OutboxStatus> statuses = List.of(OutboxStatus.WAITING_FOR_PUBLISH, OutboxStatus.FAILED);

        return jpaRepository.findReadyToDispatch(statuses, now, pageable);
    }

    @Override
    public void update(OutboxEvent event) {
        jpaRepository.save(event);
    }

    @Override
    public Optional<OutboxEvent> findByAggregateIdAndTopic(String aggregateId, String topicName) {
        return jpaRepository.findByAggregateIdAndTopicName(aggregateId, topicName);
    }
}
