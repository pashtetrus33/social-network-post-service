package ru.skillbox.social_network_post.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.dto.KafkaMessage;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.dto.KafkaDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    @Value("${spring.kafka.post-service-topic}")
    private String kafkaTopic;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Long, KafkaDto> kafkaTemplate;

    @Override
    public void produce(KafkaDto kafkaDto) {
        kafkaTemplate.send(kafkaTopic, kafkaDto);
        log.info("Sent message to Kafka -> '{}'", kafkaDto);
    }

    @KafkaListener(topics = "${spring.kafka.comment-service-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        try {
            KafkaMessage kafkaMessage = objectMapper.readValue(message, KafkaMessage.class);
            System.out.println("Получено DTO: " + kafkaMessage.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}