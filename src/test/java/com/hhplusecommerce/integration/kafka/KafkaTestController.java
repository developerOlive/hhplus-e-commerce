package com.hhplusecommerce.integration.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaTestProducer producer;

    @PostMapping("/kafka/publish")
    public ResponseEntity<String> publish(@RequestParam String message) {
        producer.sendMessage(message);
        return ResponseEntity.ok("Message sent: " + message);
    }
}
