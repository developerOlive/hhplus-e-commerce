package com.hhplusecommerce.domain.outbox.model;

import com.hhplusecommerce.domain.outbox.type.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateType;
    private String aggregateId;
    private String topicName;
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime nextRetryAt;

    public static OutboxEvent create(String aggregateType, String aggregateId, String topicName, String payload) {
        return OutboxEvent.builder()
                .id(null)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .topicName(topicName)
                .payload(payload)
                .status(OutboxStatus.WAITING_FOR_PUBLISH)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .nextRetryAt(LocalDateTime.now())
                .build();
    }

    public OutboxEvent markSuccess() {
        return this.toBuilder()
                .status(OutboxStatus.SUCCESS)
                .build();
    }

    /**
     * 재시도 실패 처리
     * - 지수 백오프 방식으로 재시도 대기 시간을 계산하여 nextRetryAt에 설정
     * - 기본 대기 시간(baseDelaySeconds)부터 시작해 재시도 횟수마다 2배씩 증가
     *   (예: 1차 재시도 30초, 2차 60초, 3차 120초, ...)
     */
    public OutboxEvent markFailed() {
        int nextRetry = retryCount + 1;
        long baseDelaySeconds = 30;
        long delaySeconds = baseDelaySeconds * (1L << (nextRetry - 1));

        return toBuilder()
                .status(OutboxStatus.FAILED)
                .retryCount(nextRetry)
                .nextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds))
                .build();
    }

    public OutboxEvent.OutboxEventBuilder toBuilder() {
        return OutboxEvent.builder()
                .id(id)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .topicName(topicName)
                .payload(payload)
                .status(status)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .nextRetryAt(nextRetryAt);
    }
}

