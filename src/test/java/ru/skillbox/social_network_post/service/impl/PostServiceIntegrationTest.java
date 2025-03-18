package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.KafkaService;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WithMockUser(username = "USER")
@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PostServiceIntegrationTest {

    private static final Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16.2-alpine")
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withNetwork(network);


    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
            .withNetwork(network);

    static {
        postgresContainer.start();
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private FriendServiceClient friendServiceClient;

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private PostRepository postRepository;

    @MockBean
    private KafkaService kafkaService;

    @BeforeEach
    void setUp() {
        // Создаем тестовую аутентификацию
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UUID testAccountId = UUID.randomUUID();

        Authentication authentication = new UsernamePasswordAuthenticationToken(testAccountId, null);
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        postRepository.deleteAll();
    }


    @WithMockUser(username = "testUser")
    @Test
    void testGetById() {
        // Arrange: Подготавливаем данные в базе
        Post post = new Post();
        post.setId(null); // ID будет сгенерирован базой данных
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
    void testGetAll() {
        // Arrange: создаем тестовый пост
        Post post = new Post();
        post.setTitle("New Test Post");
        post.setPostText("Test Content");
        post.setAuthorId(UUID.randomUUID());
        post.setPublishDate(LocalDateTime.now());
        postRepository.save(post);

        PostSearchDto searchDto = new PostSearchDto();
        searchDto.setAuthor(null);
        searchDto.setWithFriends(false);
        searchDto.setDateTo(null);
        searchDto.setAccountIds(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        PagePostDto result = postService.getAll(searchDto, pageable);

        // Assert
        assertNotNull(result, "PagePostDto should not be null");
        //assertFalse(result.getContent().isEmpty(), "Result should contain at least one post");
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

        when(accountServiceClient.getAccountsByIds(any()))
                .thenReturn(List.of(AccountDto.builder().id(accountId).build()));

        when(friendServiceClient.getFriendsIds())
                .thenReturn(List.of(friendId));

        PostSearchDto searchDto = new PostSearchDto();
        searchDto.setAuthor("test-author");
        searchDto.setWithFriends(true);

        Pageable pageable = PageRequest.of(0, 10);

        PagePostDto result = postService.getAll(searchDto, pageable);

        assertNotNull(result);
        //assertFalse(result.getContent().isEmpty());
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
}