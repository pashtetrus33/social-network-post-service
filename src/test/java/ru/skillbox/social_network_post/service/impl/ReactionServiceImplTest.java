package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.dto.ReactionNotificationDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.entity.Reaction;
import ru.skillbox.social_network_post.security.SecurityUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReactionServiceImplTest extends AbstractServiceTest {

    @Override
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

    @Test
    void testDeleteLikeFromPost() {

        // Создаем тестовый пост
        Post post = new Post();
        post.setTitle("Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(SecurityUtils.getAccountId());
        post.setPublishDate(java.time.LocalDateTime.now());
        post.setReactionsCount(1);
        post.setMyReaction(true);

        post = postRepository.save(post);

        // Создаем реакцию
        Reaction reaction = Reaction.builder()
                .authorId(SecurityUtils.getAccountId())
                .post(post)
                .type("POST")
                .reactionType("LIKE")
                .build();

        reactionRepository.save(reaction);

        // Проверяем, что реакция сохранена
        assertEquals(1, reactionRepository.count());
        assertEquals(1, postRepository.getReactionsCount(post.getId()));

        // Удаляем лайк
        reactionService.removeLikeFromPost(post.getId());

        // Проверяем, что реакция удалена
        assertEquals(0, reactionRepository.count());

        // Проверяем, что количество лайков у поста уменьшилось
        assertEquals(0, postRepository.getReactionsCount(post.getId()));
    }

    @Test
    void testRemoveLikeFromPost_WhenNoLikes_ThrowsException() {

        // Создаем тестовый пост без лайков
        Post post = new Post();
        post.setTitle("Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(SecurityUtils.getAccountId());
        post.setPublishDate(java.time.LocalDateTime.now());
        post.setReactionsCount(0);

        post = postRepository.save(post);

        // Проверяем, что нет лайков
        assertEquals(0, postRepository.getReactionsCount(post.getId()));

        // Убеждаемся, что метод выбрасывает исключение
        Post finalPost = post;
        assertThrows(IllegalStateException.class, () -> reactionService.removeLikeFromPost(finalPost.getId()));
    }
}