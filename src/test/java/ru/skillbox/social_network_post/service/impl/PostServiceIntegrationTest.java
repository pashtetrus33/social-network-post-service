package ru.skillbox.social_network_post.service.impl;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.service.KafkaService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
class PostServiceIntegrationTest extends AbstractServiceTest {

    @MockBean
    protected KafkaService kafkaService;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp(); // Вызов метода из абстрактного класса для инициализации аутентификации и очистки данных.
    }


    @Test
    void testGetById() {
        // Arrange: Подготавливаем данные в базе
        Post post = new Post();
        post.setTitle("Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        // Act: Вызываем метод getById
        PostDto postDto = postService.getById(savedPost.getId());

        // Assert: Проверяем, что результат не null и данные совпадают
        assertNotNull(postDto, "PostDto should not be null");
        assertEquals(savedPost.getId(), postDto.getId(), "Post ID should match");
        assertEquals("Test Post", postDto.getTitle(), "Post title should match");
        assertEquals("Test Content", postDto.getPostText(), "Post text should match");
    }


    @Test
    void testGetById_NotFound() {
        // Arrange: генерируем случайный UUID, который точно отсутствует в базе
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert: ожидаем выброс EntityNotFoundException
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.getById(nonExistentId),
                "Expected EntityNotFoundException to be thrown"
        );

        // Дополнительно проверим текст ошибки
        String expectedMessage = String.format("Post with id %s not found", nonExistentId);
        assertEquals(expectedMessage, exception.getMessage(), "Exception message should match");
    }

    @Test
    void testGetAll() {

        // Arrange: создаем тестовый пост
        Post post = new Post();
        post.setTitle("New Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(LocalDateTime.now(ZoneId.of("UTC")));

        postRepository.save(post);

        PostSearchDto searchDto = new PostSearchDto();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        PagePostDto result = postService.getAll(searchDto, pageable);

        // Assert
        assertNotNull(result, "PagePostDto should not be null");
        assertFalse(result.getContent().isEmpty(), "Result should contain at least one post");
    }

    @Test
    void getAll_WithAuthorAndFriendsFilters_ReturnsFilteredPosts() {
        // Сохраняем пост в БД
        Post post = new Post();
        post.setAuthorId(UUID.randomUUID());
        post.setTitle("Test title");
        post.setPostText("Test content");
        post.setPublishDate(LocalDateTime.now());
        postRepository.save(post);

        // Мокаем Feign клиентов
        UUID accountId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        when(accountServiceClient.searchAccount(anyString()))
                .thenReturn(PageAccountDto.builder()
                        .content(List.of(AccountDto.builder().id(accountId).build()))
                        .build());

        when(friendServiceClient.getFriendsIds())
                .thenReturn(List.of(friendId));

        PostSearchDto searchDto = new PostSearchDto();
        searchDto.setAuthor("test-author");
        searchDto.setWithFriends(true);

        Pageable pageable = PageRequest.of(0, 10);

        PagePostDto result = postService.getAll(searchDto, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty(), "Result should not contain posts");
    }

    @Test
    void testCreate() {
        // Arrange
        PostDto postDto = new PostDto();
        postDto.setTitle("New Test Post");
        postDto.setPostText("This is a new test post");
        postDto.setPublishDate(LocalDateTime.now());

        doNothing().when(kafkaService).newPostEvent(any(PostNotificationDto.class));

        // Act
        postService.create(postDto);

        // Assert
        Page<Post> posts = postRepository.findAll(PageRequest.of(0, 10));
        assertFalse(posts.isEmpty(), "Post should be saved in the database");
        assertEquals("New Test Post", posts.getContent().get(0).getTitle(), "Title should match");

        Mockito.verify(kafkaService, Mockito.times(1)).newPostEvent(any(PostNotificationDto.class));
    }

    @Test
    void testCreate_ShouldSetPublishDateToNow_WhenPublishDateIsNull() {
        // Arrange
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        PostDto postDto = new PostDto();
        postDto.setId(nonExistentId);
        postDto.setTitle("Title");
        postDto.setPostText("Content");

        // Act: Create post with null publishDate
        postService.create(postDto);

        doNothing().when(kafkaService).newPostEvent(any(PostNotificationDto.class));

        // Assert: Verify that publishDate was set to the current time
        assertNotNull(postDto.getPublishDate(), "Publish date should not be null");
        assertTrue(postDto.getPublishDate().isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)),
                "Publish date should be close to the current time");

        // Verify that kafkaService was called once
        Mockito.verify(kafkaService, Mockito.times(1)).newPostEvent(any(PostNotificationDto.class));
    }

    @Test
    void testUpdate() {
        // Arrange: создаем пост в базе
        Post post = new Post();
        post.setTitle("Original Title");
        post.setPostText("Original Content");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        // Обновляем DTO
        PostDto updatedDto = new PostDto();
        updatedDto.setId(savedPost.getId());
        updatedDto.setTitle("Updated Title");
        updatedDto.setPostText("Updated Content");
        updatedDto.setTimeChanged(LocalDateTime.now());

        // Act
        postService.update(updatedDto);

        // Assert
        Post updatedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertNotNull(updatedPost, "Updated post should exist");
        assertEquals("Updated Title", updatedPost.getTitle(), "Title should be updated");
        assertEquals("Updated Content", updatedPost.getPostText(), "Content should be updated");
    }

    @Test
    void testUpdate_PostNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        PostDto postDto = new PostDto();
        postDto.setId(nonExistentId);
        postDto.setTitle("Title");
        postDto.setPostText("Content");
        postDto.setTimeChanged(LocalDateTime.now());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.update(postDto),
                "Expected EntityNotFoundException to be thrown"
        );

        assertEquals("Post with id " + nonExistentId + " not found", exception.getMessage());
    }

    @Test
    void testUpdate_ShouldSetTimeChangedIfNull() {

        // Arrange: создаем пост в базе
        Post post = new Post();
        post.setTitle("Original Title");
        post.setPostText("Original Content");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        // Arrange: Создаем пустой PostDto (без timeChanged)
        PostDto postDto = new PostDto();
        postDto.setId(savedPost.getId());
        postDto.setTitle("Title");
        postDto.setPostText("Content");
        postDto.setTimeChanged(null);

        // Act: Вызываем метод update
        postService.update(postDto);

        // Assert: Проверяем, что timeChanged был установлен
        assertNotNull(postDto.getTimeChanged(), "TimeChanged should not be null");
        assertTrue(postDto.getTimeChanged().isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)), "TimeChanged should be near the current time");
    }


    @Test
    void testDelete() {
        // Arrange: создаем пост в базе
        Post post = new Post();
        post.setTitle("Delete Me");
        post.setPostText("To be deleted");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        // Act
        postService.delete(savedPost.getId());

        // Assert
        Post deletedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertNotNull(deletedPost, "Deleted post should exist in DB");
        assertTrue(deletedPost.getIsDeleted(), "Post's deleted flag should be true");
    }

    @Test
    void testUpdateBlockedStatusForAccount() {
        // Arrange: создаем посты для аккаунта
        UUID accountId = UUID.randomUUID();

        Post post1 = new Post();
        post1.setTitle("Blocked 1");
        post1.setPostText("Block test 1");
        post1.setAuthorId(accountId);
        post1.setPublishDate(LocalDateTime.now());
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Blocked 2");
        post2.setPostText("Block test 2");
        post2.setAuthorId(accountId);
        post2.setPublishDate(LocalDateTime.now());
        postRepository.save(post2);

        // Act
        postService.updateBlockedStatusForAccount(accountId);

        // Assert
        postRepository.findAll().forEach(post -> {
            if (post.getAuthorId().equals(accountId)) {
                assertTrue(post.getIsBlocked(), "Post should be blocked");
            }
        });
    }

    @Test
    void testUpdateDeletedStatusForAccount() {
        // Arrange: создаем посты для аккаунта
        UUID accountId = UUID.randomUUID();

        Post post1 = new Post();
        post1.setTitle("Delete Status 1");
        post1.setPostText("Delete status test 1");
        post1.setAuthorId(accountId);
        post1.setPublishDate(LocalDateTime.now());
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Delete Status 2");
        post2.setPostText("Delete status test 2");
        post2.setAuthorId(accountId);
        post2.setPublishDate(LocalDateTime.now());
        postRepository.save(post2);

        // Act
        postService.updateDeletedStatusForAccount(accountId);

        // Assert
        postRepository.findAll().forEach(post -> {
            if (post.getAuthorId().equals(accountId)) {
                assertTrue(post.getIsDeleted(), "Post should be marked as deleted");
            }
        });
    }

    @Test
    void testGetAuthorIds_ShouldThrowCustomFreignException() {
        // Arrange
        String author = "testAuthor";

        // Мокаем Feign client (accountServiceClient), чтобы он кидал FeignException
        when(accountServiceClient.searchAccount(author))
                .thenThrow(FeignException.class);

        // Act & Assert
        assertThrows(CustomFreignException.class, () -> postService.getAuthorIds(author));
    }

    @Test
    void testGetFriendsIds_ShouldThrowCustomFreignException() {
        // Мокаем friendServiceClient
        when(friendServiceClient.getFriendsIds())
                .thenThrow(FeignException.class);

        // Act & Assert
        assertThrows(CustomFreignException.class, () -> postService.getFriendsIds());
    }
}