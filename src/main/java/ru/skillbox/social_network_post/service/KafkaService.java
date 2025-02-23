package ru.skillbox.social_network_post.service;


import ru.skillbox.social_network_post.dto.KafkaDto;

public interface KafkaService {

    void newPostEvent(KafkaDto kafkaDto);

    void newCommentToPostEvent(KafkaDto kafkaDto);

    void newCommentToCommentEvent(KafkaDto kafkaDto);

    void newLikeToCommentEvent(KafkaDto kafkaDto);

    void newLikeToPostEvent(KafkaDto kafkaDto);
}