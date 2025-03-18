package ru.skillbox.social_network_post.service;


import ru.skillbox.social_network_post.dto.CommentNotificationDto;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.dto.PostNotificationDto;

public interface KafkaService {

    void newPostEvent(PostNotificationDto postNotificationDto);

    void newCommentEvent(CommentNotificationDto commentNotificationDto);

    void newLikeEvent(KafkaDto kafkaDto);
}