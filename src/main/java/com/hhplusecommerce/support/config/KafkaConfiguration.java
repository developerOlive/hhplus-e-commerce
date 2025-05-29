package com.hhplusecommerce.support.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Profile("!test")
@Configuration
@EnableKafka
@Slf4j
@RequiredArgsConstructor
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Kafka ProducerFactory 설정
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate 설정
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplateBean() {
        return new KafkaTemplate<>(producerFactory());
    }


    /**
     * Kafka ConsumerFactory 설정
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka 예외 처리: 3회 재시도 후 DLQ(".DLT")로 전송
     */
    @Bean
    public DefaultErrorHandler errorHandler() {
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3L);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                this.kafkaTemplate,
                (record, ex) -> {
                    String businessId = "N/A";
                    try {
                        CouponCommand command = objectMapper.readValue((String) record.value(), CouponCommand.class);
                        businessId = "userId=" + command.userId();
                    } catch (Exception e) {
                        log.warn("DLQ 전송 메시지에서 비즈니스 ID 추출 실패 (원본 메시지: {}): {}", record.value(), e.getMessage());
                    }
                    log.error("DLQ 전송 예정 메시지 실패 발생: 토픽={}, 파티션={}, 오프셋={}. 메시지={}. " +
                                    "비즈니스ID: {} 예외 종류: {} 예외 메시지: {}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.value(),
                            businessId,
                            ex.getClass().getSimpleName(),
                            ex.getMessage(),
                            ex
                    );

                    return new TopicPartition(record.topic() + ".DLT", record.partition());
                }
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Kafka 메시지 재시도 #{} 실패: 토픽={}, 파티션={}, 오프셋={}, 메시지={}. 예외: {}",
                    deliveryAttempt,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.value(),
                    ex.getMessage());
        });

        return errorHandler;
    }

    /**
     * KafkaListener 컨테이너 팩토리 설정
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }
}
