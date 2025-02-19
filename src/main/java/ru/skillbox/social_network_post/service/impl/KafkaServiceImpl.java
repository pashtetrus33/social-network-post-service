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
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.service.PostService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    @Value("${spring.kafka.post-service-topic}")
    private String postTopic;

    @Value("${spring.kafka.comment-service-topic}")
    private String commentTopic;

    @Value("${spring.kafka.blocked-account-topic}")
    private String blockedAccountTopic;

    @Value("${spring.kafka.deleted-account-topic}")
    private String deletedAccountTopic;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Long, Object> kafkaTemplate;

    private final PostService postService;


    @Override
    public void newPostEvent(KafkaDto kafkaDto) {
        kafkaTemplate.send(postTopic, kafkaDto);
        log.info("Sent new post message to Kafka -> '{}'", kafkaDto);
    }

    @Override
    public void newCommentEvent(KafkaDto kafkaDto) {
        kafkaTemplate.send(commentTopic, kafkaDto);
        log.info("Sent new comment message to Kafka -> '{}'", kafkaDto);
    }

    @Transactional
    @KafkaListener(topics = "${spring.kafka.blocked-account-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenBlockedAccount(String message) {
        try {
            AccountEventDto blockedAccountEventDto = objectMapper.readValue(message, AccountEventDto.class);
            System.out.println("Получено Blocked account DTO: " + blockedAccountEventDto.getAccountId());
            List<Post> posts = postService.getAllByAccountId(blockedAccountEventDto.getAccountId());
            posts.forEach(post -> post.setIsBlocked(true));
            postService.saveAll(posts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @KafkaListener(topics = "${spring.kafka.deleted-account-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenDeletedAccount(String message) {
        try {
            AccountEventDto accountEventDto = objectMapper.readValue(message, AccountEventDto.class);
            System.out.println("Получено Deleted account DTO: " + accountEventDto.getAccountId());

            List<Post> posts = postService.getAllByAccountId(accountEventDto.getAccountId());
            posts.forEach(post -> post.setIsDeleted(true));
            postService.saveAll(posts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}