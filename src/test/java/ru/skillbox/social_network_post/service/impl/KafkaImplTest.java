package ru.skillbox.social_network_post.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skillbox.social_network_post.dto.CommentNotificationDto;
import ru.skillbox.social_network_post.dto.PostNotificationDto;
import ru.skillbox.social_network_post.dto.ReactionNotificationDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@Testcontainers
class KafkaImplTest extends AbstractTest {

    @Autowired
    private KafkaServiceImpl kafkaService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());// Для десериализации JSON

    @MockBean  // Используйте @MockBean для mock-объекта postService
    private PostServiceImpl postService;  // Теперь это мок-объект

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    void testNewPostEvent() throws Exception {
        // Arrange
        LocalDateTime expectedPublishDate = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS); // Убираем наносекунды

        // Arrange
        PostNotificationDto postNotificationDto = PostNotificationDto.builder()
                .authorId(UUID.randomUUID())
                .postId(UUID.randomUUID())
                .title("Test Post")
                .publishDate(LocalDateTime.now(ZoneOffset.UTC)) // Дата и время
                .build();

        // Act
        kafkaService.newPostEvent(postNotificationDto);

        // Assert: Проверка с использованием KafkaConsumer
        try (KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(consumerProps())) {
            consumer.subscribe(Collections.singletonList("post.new-post")); // Подписка на топик
            ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(5)); // Ждем сообщений

            assertFalse(records.isEmpty(), "Kafka message should be received");

            // Десериализация полученного сообщения в объект PostNotificationDto
            String jsonMessage = records.iterator().next().value();
            PostNotificationDto actualPostNotificationDto = objectMapper.readValue(jsonMessage, PostNotificationDto.class);

            // Сравнение с ожидаемым объектом
            assertEquals(postNotificationDto.getAuthorId(), actualPostNotificationDto.getAuthorId());
            assertEquals(postNotificationDto.getPostId(), actualPostNotificationDto.getPostId());
            assertEquals(postNotificationDto.getTitle(), actualPostNotificationDto.getTitle());
            assertEquals(expectedPublishDate, actualPostNotificationDto.getPublishDate().truncatedTo(ChronoUnit.SECONDS)); // Сравниваем до секунд
        }
    }

    @Test
    void testNewCommentEvent() throws Exception {
        // Arrange
        CommentNotificationDto commentNotificationDto = CommentNotificationDto.builder()
                .commentId(UUID.randomUUID())
                .postId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .shortCommentText("Test comment")
                .build();

        // Act
        kafkaService.newCommentEvent(commentNotificationDto);

        // Assert: Проверяем, что KafkaTemplate был вызван
        try (KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(consumerProps())) {
            consumer.subscribe(Collections.singletonList("post.new-comment"));
            ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(5));

            assertFalse(records.isEmpty(), "Kafka message should be received");

            // Десериализация полученного сообщения
            String jsonMessage = records.iterator().next().value();
            CommentNotificationDto actualCommentNotificationDto = objectMapper.readValue(jsonMessage, CommentNotificationDto.class);

            // Сравниваем поля
            assertEquals(commentNotificationDto.getCommentId(), actualCommentNotificationDto.getCommentId());
            assertEquals(commentNotificationDto.getPostId(), actualCommentNotificationDto.getPostId());
            assertEquals(commentNotificationDto.getAuthorId(), actualCommentNotificationDto.getAuthorId());
            assertEquals(commentNotificationDto.getShortCommentText(), actualCommentNotificationDto.getShortCommentText());
        }
    }

    @Test
    void testNewLikeEvent() throws Exception {
        // Arrange
        ReactionNotificationDto reactionNotificationDto = ReactionNotificationDto.builder()
                .reactionId(UUID.randomUUID())
                .postId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .reactionType("LIKE")
                .build();

        // Act
        kafkaService.newLikeEvent(reactionNotificationDto);

        // Assert: Проверяем, что KafkaTemplate был вызван
        try (KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(consumerProps())) {
            consumer.subscribe(Collections.singletonList("post.new-like"));
            ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(5));

            assertFalse(records.isEmpty(), "Kafka message should be received");

            // Десериализация полученного сообщения
            String jsonMessage = records.iterator().next().value();
            ReactionNotificationDto actualReactionNotificationDto = objectMapper.readValue(jsonMessage, ReactionNotificationDto.class);

            // Сравниваем поля
            assertEquals(reactionNotificationDto.getReactionId(), actualReactionNotificationDto.getReactionId());
            assertEquals(reactionNotificationDto.getPostId(), actualReactionNotificationDto.getPostId());
            assertEquals(reactionNotificationDto.getAuthorId(), actualReactionNotificationDto.getAuthorId());
            assertEquals(reactionNotificationDto.getReactionType(), actualReactionNotificationDto.getReactionType());
        }
    }

    @Test
    void testListenBlockedAccount() {
        // Arrange
        String message = "{\"accountId\":\"" + UUID.randomUUID() + "\"}";

        // Act: Симулируем обработку сообщения
        kafkaService.listenBlockedAccount(message);

        // Assert: Проверяем, что сервис обновил статус блокировки
        verify(postService, times(1)).updateBlockedStatusForAccount(any(UUID.class));
    }

    @Test
    void testListenDeletedAccount() {
        // Arrange
        String message = "{\"accountId\":\"" + UUID.randomUUID() + "\"}";

        // Act: Симулируем обработку сообщения
        kafkaService.listenDeletedAccount(message);

        // Assert: Проверяем, что сервис обновил статус удаления
        verify(postService, times(1)).updateDeletedStatusForAccount(any(UUID.class));
    }
}
