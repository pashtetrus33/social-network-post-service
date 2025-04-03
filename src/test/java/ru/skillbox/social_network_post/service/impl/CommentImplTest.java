package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.CommentType;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.service.KafkaService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

class CommentImplTest extends AbstractTest {

    @MockBean
    protected KafkaService kafkaService;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp(); // Вызов метода из абстрактного класса для инициализации аутентификации и очистки данных.
    }

    @Override
    protected void clearRepositoryData() {
        // Очистка данных перед каждым тестом
        commentRepository.deleteAll();
    }


    @Test
    void testCreateComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentText("Test Comment");
        commentDto.setTime(LocalDateTime.now());

        // Arrange: создаём пост
        Post post = Post.builder()
                .title("New Test Post")
                .postText("This is a new test post")
                .publishDate(LocalDateTime.now())
                .commentsCount(0L)  // Изначально 0 комментариев
                .build();
        post = postRepository.save(post);

        // Мокируем вызов Kafka
        doNothing().when(kafkaService).newCommentEvent(any());

        // Act: создаём комментарий
        commentService.create(post.getId(), commentDto);

        // Assert 1: Проверяем, что комментарий сохранён в репозитории
        Comment createdComment = commentRepository.findAll().get(0);
        Assertions.assertNotNull(createdComment, "Комментарий должен сохраниться в БД");

        Assertions.assertEquals(commentDto.getCommentText(), createdComment.getCommentText(), "Текст комментария должен совпадать");
        Assertions.assertEquals(post.getId(), createdComment.getPost().getId(), "Комментарий должен быть связан с правильным постом");

        // Assert 2: Проверяем, что у поста увеличился счётчик комментариев
        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        Assertions.assertEquals(1, updatedPost.getCommentsCount(), "Количество комментариев должно быть 1");

        // Assert 3: Проверяем вызов Kafka
        verify(kafkaService, times(1)).newCommentEvent(any());
    }


    @Test
    void testGetByPostId() {
        // Создаем пост
        Post post = Post.builder()
                .title("Test Post")
                .postText("Some text")
                .publishDate(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        // Сохраняем несколько комментариев к посту
        for (int i = 1; i <= 3; i++) {
            Comment comment = Comment.builder()
                    .id(UUID.randomUUID())
                    .post(post)
                    .commentText("Comment " + i)
                    .commentType(CommentType.POST)
                    .isBlocked(false)
                    .isDeleted(false)
                    .time(LocalDateTime.now())
                    .build();
            commentRepository.save(comment);
        }

        // Подготавливаем параметры поиска и пагинации
        CommentSearchDto searchDto = new CommentSearchDto();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("time").descending());

        // Вызов сервиса
        PageCommentDto pageCommentDto = commentService.getByPostId(post.getId(), searchDto, pageable);

        // Проверки
        Assertions.assertNotNull(pageCommentDto);
        Assertions.assertEquals(Long.valueOf(3), pageCommentDto.getTotalElements());
        Assertions.assertEquals(Integer.valueOf(1), pageCommentDto.getTotalPages());
        Assertions.assertEquals(3L, pageCommentDto.getContent().size());

        // Доп. Проверки содержимого
        for (CommentDto dto : pageCommentDto.getContent()) {
            Assertions.assertEquals(post.getId(), dto.getPostId());
            Assertions.assertNotNull(dto.getCommentText());
        }
    }


    @Test
    void testGetSubcomments() {
        // Arrange: Создаем пост
        Post post = Post.builder()
                .title("Test Post")
                .postText("Post text")
                .publishDate(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        // Создаем родительский комментарий
        Comment parentComment = Comment.builder()
                .commentText("Parent Comment")
                .post(post)
                .commentType(CommentType.POST)
                .isBlocked(false)
                .isDeleted(false)
                .build();
        commentRepository.save(parentComment);

        parentComment = commentRepository.findAll().get(0);

        // Создаем подкомментарий
        Comment subComment = Comment.builder()
                .commentText("Subcomment")
                .post(post)
                .parentComment(parentComment)
                .commentType(CommentType.POST)
                .isBlocked(false)
                .isDeleted(false)
                .build();
        commentRepository.save(subComment);

        // Act: вызываем метод
        Pageable pageable = PageRequest.of(0, 10);
        PageCommentDto result = commentService.getSubcomments(post.getId(), parentComment.getId(), pageable);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Long.valueOf(1), result.getTotalElements()); // Должен быть 1 подкомментарий
        Assertions.assertEquals(subComment.getCommentText(), result.getContent().get(0).getCommentText());
    }

    @Test
    void testGetSubcomments_CommentNotFound() {
        // Arrange: Создаем пост
        Post post = Post.builder()
                .title("Test Post")
                .postText("Post text")
                .publishDate(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        // Act & Assert: Проверяем выброс исключения, если комментарий не найден
        UUID postId = post.getId();
        Pageable pageable = PageRequest.of(0, 10);
        UUID uuid = UUID.randomUUID();

        assertThrows(EntityNotFoundException.class, () -> commentService.getSubcomments(postId, uuid, pageable));
    }

    @Test
    void testGetSubcomments_PostNotFound() {
        // Arrange: Создаем комментарий
        Post post = Post.builder()
                .title("Test Post")
                .postText("Post text")
                .publishDate(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        Comment parentComment = Comment.builder()
                .id(UUID.randomUUID())
                .commentText("Parent Comment")
                .post(post)  // Привязываем к существующему посту
                .commentType(CommentType.POST)
                .isBlocked(false)
                .isDeleted(false)
                .build();
        commentRepository.save(parentComment);

        UUID postId = UUID.randomUUID();
        UUID id = parentComment.getId();
        PageRequest pageable = PageRequest.of(0, 10);

        // Act & Assert: Проверяем выброс исключения, если пост не найден
        assertThrows(EntityNotFoundException.class, () -> commentService.getSubcomments(postId, id, pageable));
    }


    @Test
    void testUpdateComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentText("Test Comment");
        commentDto.setTime(LocalDateTime.now());
        commentDto.setCommentType(CommentType.POST);
        commentDto.setIsBlocked(false);
        commentDto.setIsDeleted(false);
        commentDto.setMyLike(false);

        // Arrange: создаём пост
        Post post = Post.builder()
                .title("New Test Post")
                .postText("This is a new test post")
                .publishDate(LocalDateTime.now())
                .commentsCount(0L)  // Изначально 0 комментариев
                .build();
        post = postRepository.save(post);

        // Мокируем вызов Kafka
        doNothing().when(kafkaService).newCommentEvent(any());

        // Act: создаём комментарий
        commentService.create(post.getId(), commentDto);

        // Assert 1: Проверяем, что комментарий сохранён в репозитории
        Comment createdComment = commentRepository.findAll().get(0);
        Assertions.assertNotNull(createdComment, "Комментарий должен сохраниться в БД");

        // Act: обновляем комментарий
        CommentDto updatedCommentDto = new CommentDto();
        updatedCommentDto.setId(createdComment.getId());
        updatedCommentDto.setCommentText("Updated Comment");
        updatedCommentDto.setCommentType(CommentType.POST);
        updatedCommentDto.setIsBlocked(false);
        updatedCommentDto.setIsDeleted(false);
        updatedCommentDto.setMyLike(false);

        commentService.update(post.getId(), createdComment.getId(), updatedCommentDto);

        // Assert: проверяем, что комментарий обновился
        Comment updatedComment = commentRepository.findById(createdComment.getId()).orElseThrow();
        Assertions.assertEquals("Updated Comment", updatedComment.getCommentText(),
                "Текст комментария должен быть обновлён");
    }


    @Test
    void testUpdateCommentIdMismatch() {
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto commentDto = new CommentDto();
        commentDto.setId(UUID.randomUUID());  // Другой ID для комментария

        assertThrows(IdMismatchException.class, () -> commentService.update(postId, commentId, commentDto));
    }

    @Test
    void testDeleteComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentText("Test Comment");
        commentDto.setTime(LocalDateTime.now());

        // Arrange: создаём пост
        Post post = Post.builder()
                .title("New Test Post")
                .postText("This is a new test post")
                .publishDate(LocalDateTime.now())
                .commentsCount(0L)  // Изначально 0 комментариев
                .build();
        post = postRepository.save(post);

        // Мокируем вызов Kafka
        doNothing().when(kafkaService).newCommentEvent(any());

        // Act: создаём комментарий
        commentService.create(post.getId(), commentDto);

        // Assert 1: Проверяем, что комментарий сохранён в репозитории
        Comment createdComment = commentRepository.findAll().get(0);
        Assertions.assertNotNull(createdComment, "Комментарий должен сохраниться в БД");

        Assertions.assertFalse(createdComment.getIsDeleted(), "Комментарий не должен быть удалён сразу после создания");

        // Act: удаляем комментарий
        commentService.delete(post.getId(), createdComment.getId());

        // Assert 2: Проверяем, что комментарий помечен как удалённый
        Comment deletedComment = commentRepository.findById(createdComment.getId()).orElseThrow();
        Assertions.assertTrue(deletedComment.getIsDeleted(), "Комментарий должен быть помечен как удалённый");

        // Assert 3: Проверяем, что счётчик комментариев уменьшился
        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        Assertions.assertEquals(0, updatedPost.getCommentsCount(), "Счётчик комментариев должен уменьшиться");
    }
}