package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.dto.ReactionNotificationDto;
import ru.skillbox.social_network_post.dto.RequestReactionDto;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.CommentType;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.entity.Reaction;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.KafkaService;

import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReactionImplTest extends AbstractTest {

    @MockBean
    protected KafkaService kafkaService;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    void testAddLikeToOwnPost() {
        // Arrange: Создаем тестовый пост
        Post post = createTestPost(SecurityUtils.getAccountId());

        post = postRepository.save(post);

        // Arrange: создаем валидный ReactionDto
        RequestReactionDto requestReactionDto = createTestRequestReactionDto();

        // Act
        ReactionDto result = reactionService.addLikeToPost(post.getId(), requestReactionDto);

        post = postRepository.findAll().get(0);

        doNothing().when(kafkaService).newLikeEvent(any(ReactionNotificationDto.class));

        // Assert: Проверяем, что лайк добавлен и уведомление отправлено
        assertNotNull(result);
        assertEquals("delight", result.getReactionsInfo().get(0).getReactionType());
        assertEquals(1, result.getQuantity());  // Проверяем, что лайков 1
        assertEquals(1, post.getReactionsCount());  // Проверяем, что у поста лайков 1
        verify(kafkaService, times(1)).newLikeEvent(any());
    }

    @Test
    void testAddLikeToNotOwnPost() {

        // Arrange: Создаем тестовый пост
        Post post = createTestPost(UUID.randomUUID());

        post = postRepository.save(post);

        // Arrange: создаем валидный ReactionDto
        RequestReactionDto requestReactionDto = createTestRequestReactionDto();

        // Act
        ReactionDto result = reactionService.addLikeToPost(post.getId(), requestReactionDto);

        post = postRepository.findAll().get(0);

        doNothing().when(kafkaService).newLikeEvent(any(ReactionNotificationDto.class));

        // Assert: Проверяем, что лайк добавлен и уведомление отправлено
        assertNotNull(result);
        assertEquals("delight", result.getReactionsInfo().get(0).getReactionType());
        assertEquals(1, result.getQuantity());  // Проверяем, что лайков 1
        assertEquals(1, post.getReactionsCount());  // Проверяем, что у поста лайков 1
        verify(kafkaService, times(1)).newLikeEvent(any());
    }


    @Test
    void testDeleteLikeFromPost() {

        // Создаем тестовый пост
        Post post = createTestPost(UUID.randomUUID());
        post.setReactionsCount(1L);

        post = postRepository.save(post);

        // Создаем реакцию
        Reaction reaction = createTestReaction(post, null, SecurityUtils.getAccountId());

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
        Post post = createTestPost(UUID.randomUUID());
        post.setReactionsCount(0L);

        post = postRepository.save(post);

        // Проверяем, что нет лайков
        assertEquals(0, postRepository.getReactionsCount(post.getId()));

        // Убеждаемся, что метод выбрасывает исключение

        UUID postId = post.getId();
        assertThrows(IllegalStateException.class, () -> reactionService.removeLikeFromPost(postId));
    }

    @Test
    void testAddLikeToComment() {
        // Arrange: Создаем тестовый пост
        Post post = createTestPost(UUID.randomUUID());
        post = postRepository.save(post);

        // Создаем тестовый комментарий
        Comment comment = commentRepository.save(createTestComment(post, SecurityUtils.getAccountId()));

        // Act
        reactionService.addLikeToComment(post.getId(), comment.getId());

        // Assert
        var reactions = reactionRepository.findAll();
        assertEquals(1, reactions.size());

        Reaction reaction = reactions.get(0);
        assertEquals(post.getId(), reaction.getPost().getId());
        assertEquals(comment.getId(), reaction.getCommentId());
        assertEquals(SecurityUtils.getAccountId(), reaction.getAuthorId());

        verify(kafkaService, times(1)).newLikeEvent(any(ReactionNotificationDto.class));
    }

    @Test
    void testAddLikeToComment_WhenAlreadyExists_DoesNotAddDuplicateReaction() {
        // Arrange
        Post post = postRepository.save(createTestPost(UUID.randomUUID()));
        Comment comment = commentRepository.save(createTestComment(post, SecurityUtils.getAccountId()));

        // Добавляем лайк
        reactionRepository.save(createTestReaction(post, comment.getId(), SecurityUtils.getAccountId()));

        UUID commentId = comment.getId();
        UUID postId = post.getId();

        // Проверяем количество реакций до вызова метода
        long beforeCount = reactionRepository.count();

        // Act
        reactionService.addLikeToComment(postId, commentId);

        // Assert
        long afterCount = reactionRepository.count();
        assertEquals(beforeCount, afterCount, "Количество реакций не должно увеличиваться при повторном лайке");
    }


    @Test
    void testRemoveLikeFromComment() {
        // Arrange
        Post post = createTestPost(UUID.randomUUID());
        post = postRepository.save(post);

        var comment = commentRepository.save(createTestComment(post, SecurityUtils.getAccountId()));
        comment.setLikeAmount(1);
        commentRepository.save(comment);

        Reaction reaction = createTestReaction(post, comment.getId(), SecurityUtils.getAccountId());
        reactionRepository.save(reaction);

        assertEquals(1, commentRepository.getLikeAmount(comment.getId()));

        // Act
        reactionService.removeLikeFromComment(post.getId(), comment.getId());

        // Assert
        assertEquals(0, reactionRepository.count());
        assertEquals(0, commentRepository.getLikeAmount(comment.getId()));
    }


    @Test
    void testRemoveLikeFromComment_WhenNoLikes_ThrowsException() {
        // Arrange
        Post post = createTestPost(UUID.randomUUID());
        post = postRepository.save(post);

        Comment comment = commentRepository.save(createTestComment(post, SecurityUtils.getAccountId()));
        comment.setLikeAmount(0);
        commentRepository.save(comment);

        // Act + Assert
        UUID postId = post.getId();
        UUID commentId = comment.getId();
        assertThrows(IllegalStateException.class, () -> reactionService.removeLikeFromComment(postId, commentId));
    }

    private static Post createTestPost(UUID authorId) {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(authorId);
        post.setPublishDate(java.time.LocalDateTime.now(ZoneOffset.UTC));
        return post;
    }

    private static Comment createTestComment(Post post, UUID authorId) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setCommentType(CommentType.COMMENT);
        comment.setIsBlocked(false);
        comment.setIsDeleted(false);
        comment.setAuthorId(authorId);
        comment.setCommentText("Test Comment");
        comment.setTime(java.time.LocalDateTime.now(ZoneOffset.UTC));
        return comment;
    }

    private static Reaction createTestReaction(Post post, UUID commentId, UUID authorId) {
        Reaction reaction = new Reaction();
        reaction.setPost(post);
        reaction.setCommentId(commentId);
        reaction.setAuthorId(authorId);
        reaction.setType("POST");
        reaction.setReactionType("DELIGHT");
        return reaction;
    }

    private static RequestReactionDto createTestRequestReactionDto() {
        return RequestReactionDto.builder()
                .reactionType("delight")
                .type("POST")
                .build();
    }
}