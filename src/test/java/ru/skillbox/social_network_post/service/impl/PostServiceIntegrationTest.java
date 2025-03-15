package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostSearchDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.KafkaService;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@WithMockUser(username = "USER")
@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
class PostServiceIntegrationTest {

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
    void testCreate() {
        // Arrange
        PostDto postDto = new PostDto();
        postDto.setTitle("New Test Post");
        postDto.setPostText("This is a new test post");
        postDto.setPublishDate(LocalDateTime.now());

        doNothing().when(kafkaService).newPostEvent(any(KafkaDto.class));

        // Act
        postService.create(postDto);

        // Assert
        Page<Post> posts = postRepository.findAll(PageRequest.of(0, 10));
        assertFalse(posts.isEmpty(), "Post should be saved in the database");
        assertEquals("New Test Post", posts.getContent().get(1).getTitle(), "Title should match");

        Mockito.verify(kafkaService, Mockito.times(1)).newPostEvent(any(KafkaDto.class));
    }
}