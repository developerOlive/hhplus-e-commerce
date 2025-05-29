package com.hhplusecommerce.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class TestcontainersConfiguration {

    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:6.2.5");
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:latest");

    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(MYSQL_IMAGE)
            .withReuse(true)
            .withDatabaseName("hhplus")
            .withUsername("test")
            .withPassword("test");

    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(REDIS_IMAGE)
            .withReuse(true)
            .withExposedPorts(6379);

    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(KAFKA_IMAGE)
            .withExposedPorts(9093);

    static {
        MYSQL_CONTAINER.start();
        REDIS_CONTAINER.start();
        KAFKA_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String redisHost = REDIS_CONTAINER.getHost();
        Integer redisPort = REDIS_CONTAINER.getMappedPort(6379);

        registry.add("spring.datasource.url", () ->
                MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

        registry.add("spring.redis.host", () -> redisHost);
        registry.add("spring.redis.port", () -> redisPort);
        registry.add("redisson.address", () -> "redis://" + redisHost + ":" + redisPort);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }
}
