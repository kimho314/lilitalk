package com.luna.demokafka.controller;

import com.luna.demokafka.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);
    private final KafkaProducerService producerService;

    public KafkaController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/send")
    public void sendMessage(
        @RequestParam String topic,
        @RequestParam(required = false) String key,
        @RequestParam String message
    ) {
        producerService.sendMessage(topic, key, message);
    }
}
