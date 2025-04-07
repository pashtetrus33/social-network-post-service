package ru.skillbox.social_network_post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostNotificationDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.impl.AbstractTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class PostControllerIntegrationTest extends AbstractTest {

    @MockBean
    KafkaService kafkaService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    private Post testPost;
    private final String fakeToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWNjb3VudElkIjoiMTc4ODBkMmUtMjJjYy00ODQwLTgyNGItNWQyMWNmMWZjNDc4IiwiYWRtaW4iOnRydWUsImlhdCI6MTc0MzY4NzQwMiwiZXhwIjoxNzQzNjkxMDAyfQ.LvfPVBQN84KNmRznEaCwfrEUFfiAl3DMwGKYN-Q9lso"; // Фейковый токен

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        testPost = new Post();
        testPost.setId(UUID.randomUUID());
        testPost.setTitle("Test Post");
        testPost.setPostText("This is a test post");
        testPost.setPublishDate(LocalDateTime.now(ZoneOffset.UTC));

        // Мокаем поведение валидации токена
        when(authServiceClient.validateToken(anyString())).thenReturn(true);

        doNothing().when(kafkaService).newPostEvent(any(PostNotificationDto.class));
    }

    @Test
    void testCreatePost() throws Exception {
        PostDto postDto = new PostDto();
        postDto.setTitle("Test Post");
        postDto.setPostText("This is a test post");
        postDto.setPublishDate(LocalDateTime.now(ZoneOffset.UTC));

        mockMvc.perform(post("/api/v1/post")
                        .header(HttpHeaders.AUTHORIZATION, fakeToken) // Добавляем фейковый токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetPostById() throws Exception {
        var post = postRepository.save(testPost);

        mockMvc.perform(get("/api/v1/post/" + post.getId())
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)) // Добавляем фейковый токен
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    void testUpdatePost() throws Exception {
        var post = postRepository.save(testPost);

        PostDto updatedDto = new PostDto();
        updatedDto.setId(post.getId());
        updatedDto.setTitle("Updated Title");
        updatedDto.setPostText("Updated Content");

        mockMvc.perform(put("/api/v1/post")
                        .header(HttpHeaders.AUTHORIZATION, fakeToken) // Добавляем фейковый токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePost() throws Exception {
        var post = postRepository.save(testPost);

        mockMvc.perform(delete("/api/v1/post/" + post.getId())
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)) // Добавляем фейковый токен
                .andExpect(status().isNoContent());
    }
}