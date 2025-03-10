package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PostServiceIntegrationTest {

    // Создаем статический контейнер и подавляем предупреждение,
    // поскольку мы управляем его жизненным циклом вручную (статический блок ниже)
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.2-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    // Статический блок запуска контейнера
    static {
        postgresContainer.start();
    }

    // Регистрируем динамические свойства, чтобы Spring Boot использовал параметры контейнера
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private PostRepository postRepository;

    // Тест метода getById
    @Test
    public void testGetById() {
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
}