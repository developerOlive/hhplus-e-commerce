package com.hhplusecommerce.infrastructure.coupon.kafka;

import com.hhplusecommerce.application.coupon.CouponIssueProcessor;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka로부터 쿠폰 발급 요청 메시지를 수신하고 처리
 * Redis 없이 DB 기반으로 처리되며, 실패 시 DLQ로 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCouponIssueConsumer {

    private final CouponIssueProcessor couponIssueProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.coupon-issue-request}",
            groupId = "${custom.kafka.consumer.group-id.coupon-processor}"
    )
    public void listenCouponIssueRequest(String message) {
        CouponCommand command = null;

        try {
            command = objectMapper.readValue(message, CouponCommand.class);
            couponIssueProcessor.processCouponIssue(command.userId(), command.couponId());

        } catch (Exception e) {
            String userId = (command != null && command.userId() != null) ? String.valueOf(command.userId()) : "unknown";
            String couponId = (command != null && command.couponId() != null) ? String.valueOf(command.couponId()) : "unknown";

            log.warn("Kafka 쿠폰 발급 처리 실패 - couponId={}, userId={}, message={}, 예외={}",
                    couponId,
                    userId,
                    message,
                    e.getMessage(),
                    e
            );

            // Kafka DLQ 전송 유도
            throw new RuntimeException("Kafka Consumer 처리 실패", e);
        }
    }
}
