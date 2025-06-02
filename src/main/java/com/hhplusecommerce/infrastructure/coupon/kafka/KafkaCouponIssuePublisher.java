package com.hhplusecommerce.infrastructure.coupon.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.port.out.CouponIssuePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka를 사용하여 쿠폰 발급 요청 메시지를 발행
 */
@RequiredArgsConstructor
@Service
public class KafkaCouponIssuePublisher implements CouponIssuePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${custom.kafka.topic.coupon-issue-request}")
    private String topicName;

    @Override
    public void publishCouponRequest(CouponCommand command) {
        try {
            String payload = objectMapper.writeValueAsString(command);
            kafkaTemplate.send(topicName, command.couponId().toString(), payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("쿠폰 발급 요청 직렬화 실패", e);
        }
    }
}
