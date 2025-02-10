package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.web.model.KafkaDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    @Value("${spring.kafka.post-service-topic}")
    private String kafkaTopic;

    private final KafkaTemplate<Long, KafkaDto> kafkaTemplate;

    @Override
    public void produce(KafkaDto kafkaDto) {
        kafkaTemplate.send(kafkaTopic, kafkaDto);
        log.info("Sent message to Kafka -> '{}'", kafkaDto);
    }
}