package ru.skillbox.social_network_post.service;


import ru.skillbox.social_network_post.dto.KafkaDto;

import java.util.UUID;

public interface KafkaService {

    void newPostEvent(KafkaDto kafkaDto);

    void newCommentEvent(KafkaDto kafkaDto);

    void deletedAccountEvent(UUID accountId);

    void blockedAccountEvent(UUID accountId);
}