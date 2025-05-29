package com.hhplusecommerce.integration.kafka;

import com.hhplusecommerce.support.IntegrationTestSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class KafkaIntegrationTest extends IntegrationTestSupport {

    private static final String TEST_MESSAGE = "hello kafka";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaTestConsumer kafkaTestConsumer;

    @BeforeEach
    void resetConsumer() {
        kafkaTestConsumer.reset();
    }

    @Test
    void verifyMessageReceivedOnTestTopic() throws Exception {
        mockMvc.perform(post("/kafka/publish")
                        .param("message", TEST_MESSAGE))
                .andExpect(status().isOk());

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(kafkaTestConsumer.getMessageCount()).isGreaterThan(0));
    }
}
