package com.hhplusecommerce.domain.outbox.service;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.port.OutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventPort outboxEventPort;

    public void save(OutboxEvent event) {
        outboxEventPort.save(event);
    }

    public Optional<OutboxEvent> findByAggregateIdAndTopic(String aggregateId, String topicName) {
        return outboxEventPort.findByAggregateIdAndTopic(aggregateId, topicName);
    }

}
