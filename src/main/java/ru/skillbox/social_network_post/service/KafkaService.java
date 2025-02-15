package ru.skillbox.social_network_post.service;


import ru.skillbox.social_network_post.dto.KafkaDto;

public interface KafkaService {

    void produce(KafkaDto kafkaDto);
}