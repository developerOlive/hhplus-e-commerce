package com.hhplusecommerce.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Testcontainers
@Profile("test")
@Configuration
public class KafkaTestSupport {

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.4.0");

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(KAFKA_IMAGE)
            .withReuse(true)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
            .withLogConsumer(new Slf4jLogConsumer(log));

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServersFallback;

    @BeforeAll
    static void setupKafka() {
        KAFKA_CONTAINER.start();
        createKafkaTopics(KAFKA_CONTAINER.getBootstrapServers(), "order.completed.v1", "test-topic");
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    private static void createKafkaTopics(String bootstrapServers, String... topicNames) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(props)) {
            List<NewTopic> topics = Arrays.stream(topicNames)
                    .map(name -> new NewTopic(name, 1, (short) 1))
                    .collect(Collectors.toList());
            adminClient.createTopics(topics).all().get();
            log.info("Kafka topics created: {}", topics.stream().map(NewTopic::name).collect(Collectors.joining(", ")));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to create Kafka test topics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Kafka test topics", e);
        } catch (Exception e) {
            log.error("Failed to create Kafka test topics (general exception): {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Kafka test topics", e);
        }
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        String bootstrapServers = KAFKA_CONTAINER.isRunning() ? KAFKA_CONTAINER.getBootstrapServers() : bootstrapServersFallback;

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
