package ru.skillbox.social_network_post.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.dto.AccountEventDto;
import ru.skillbox.social_network_post.dto.PostNotificationDto;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.service.PostService;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    @Value("${spring.kafka.new-post-topic}")
    private String newPostTopic;

    @Value("${spring.kafka.new-comment-topic}")
    private String newCommentTopic;

    @Value("${spring.kafka.new-like-topic}")
    private String newLikeTopic;

    @Value("${spring.kafka.blocked-account-topic}")
    private String blockedAccountTopic;

    @Value("${spring.kafka.deleted-account-topic}")
    private String deletedAccountTopic;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Long, Object> kafkaTemplate;

    private final PostService postService;


    @Override
    public void newPostEvent(PostNotificationDto postNotificationDto) {
        kafkaTemplate.send(newPostTopic, postNotificationDto);
        log.info("Sent new post -> '{}'", postNotificationDto);
    }


    @Override
    public void newCommentEvent(KafkaDto kafkaDto) {
        kafkaTemplate.send(newCommentTopic, kafkaDto);
        log.info("Sent new comment -> '{}'", kafkaDto);
    }


    @Override
    public void newLikeEvent(KafkaDto kafkaDto) {
        kafkaTemplate.send(newLikeTopic, kafkaDto);
        log.info("Sent new like -> '{}'", kafkaDto);
    }


    @Transactional
    @KafkaListener(topics = "${spring.kafka.blocked-account-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenBlockedAccount(String message) {
        processAccountEvent(message, "Blocked");
    }


    @Transactional
    @KafkaListener(topics = "${spring.kafka.deleted-account-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDeletedAccount(String message) {
        processAccountEvent(message, "Deleted");
    }


    private void processAccountEvent(String message, String accountStatus) {
        try {
            AccountEventDto accountEventDto = objectMapper.readValue(message, AccountEventDto.class);
            log.info("Account {}: {}", accountStatus, accountEventDto.accountId());

            if (accountStatus.equals("Blocked")) {
                postService.updateBlockedStatusForAccount(accountEventDto.accountId());
            } else if (accountStatus.equals("Deleted")) {
                postService.updateDeletedStatusForAccount(accountEventDto.accountId());
            }

        } catch (Exception e) {
            log.error("Error processing {} account message: {}", accountStatus, message, e);
        }
    }
}