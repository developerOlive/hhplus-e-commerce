package com.hhplusecommerce.infrastructure.outbox;

import com.hhplusecommerce.domain.outbox.model.OutboxEvent;
import com.hhplusecommerce.domain.outbox.type.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {
    @Query("""
    SELECT o FROM OutboxEvent o
    WHERE o.status IN :statuses
      AND o.nextRetryAt <= :now
    ORDER BY o.createdAt ASC
    """)
    List<OutboxEvent> findReadyToDispatch(
            @Param("statuses") List<OutboxStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    Optional<OutboxEvent> findByAggregateIdAndTopicName(String aggregateId, String topicName);

}
