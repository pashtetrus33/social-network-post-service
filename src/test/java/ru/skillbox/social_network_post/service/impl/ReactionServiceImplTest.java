package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.dto.ReactionNotificationDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.security.SecurityUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReactionServiceImplTest extends AbstractServiceTest {

    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @Test
    void testAddLikeToOwnPost() {
        // Arrange: Создаем тестовый пост

        UUID accountId = SecurityUtils.getAccountId();

        Post post = new Post();
        post.setTitle("Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(accountId);
        post.setPublishDate(java.time.LocalDateTime.now());

        post = postRepository.save(post);

        // Arrange: создаем валидный ReactionDto
        ReactionDto reactionDto = ReactionDto.builder()
                .type("POST")
                .reactionType("LIKE")
                .build();

        // Act
        ReactionDto result = reactionService.addLikeToPost(post.getId(), reactionDto);

        post = postRepository.findAll().get(0);

        doNothing().when(kafkaService).newLikeEvent(any(ReactionNotificationDto.class));

        // Assert: Проверяем, что лайк добавлен и уведомление отправлено
        assertNotNull(result);
        assertEquals("LIKE", result.getReactionType());
        assertEquals(1, result.getCount());  // Проверяем, что лайков 1
        assertEquals(1, post.getReactionsCount());  // Проверяем, что у поста лайков 1
        assertTrue(post.getMyReaction());
        verify(kafkaService, times(1)).newLikeEvent(any());
    }

    @Test
    void testAddLikeToNotOwnPost() {

        // Arrange: Создаем тестовый пост
        Post post = new Post();
        post.setTitle("Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(java.time.LocalDateTime.now());

        post = postRepository.save(post);

        // Arrange: создаем валидный ReactionDto
        ReactionDto reactionDto = ReactionDto.builder()
                .type("POST")
                .reactionType("LIKE")
                .build();

        // Act
        ReactionDto result = reactionService.addLikeToPost(post.getId(), reactionDto);

        post = postRepository.findAll().get(0);

        doNothing().when(kafkaService).newLikeEvent(any(ReactionNotificationDto.class));

        // Assert: Проверяем, что лайк добавлен и уведомление отправлено
        assertNotNull(result);
        assertEquals("LIKE", result.getReactionType());
        assertEquals(1, result.getCount());  // Проверяем, что лайков 1
        assertEquals(1, post.getReactionsCount());  // Проверяем, что у поста лайков 1
        assertFalse(post.getMyReaction());
        verify(kafkaService, times(1)).newLikeEvent(any());
    }
}