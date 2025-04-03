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
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.CommentType;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.entity.Reaction;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.impl.AbstractTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class CommentControllerIntegrationTest extends AbstractTest {

    @MockBean
    KafkaService kafkaService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private UUID postId;
    private UUID commentId;
    private CommentDto testCommentDto;
    private Comment testComment;
    private Post testPost;

    private final String fakeToken =
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWNjb3VudElkIjoiMTc4ODBkMmUtMjJjYy00ODQwLTgyNGItNWQyMWNmMWZjNDc4IiwiYWRtaW4iOnRydWUsImlhdCI6MTc0MzY4NzQwMiwiZXhwIjoxNzQzNjkxMDAyfQ.LvfPVBQN84KNmRznEaCwfrEUFfiAl3DMwGKYN-Q9lso"; // Фейковый токен

    @BeforeEach
    protected void setUp() {

        // Создаем пост в базе данных
        testPost = Post.builder()
                .title("Test Post")
                .postText("Content of test post")
                .publishDate(LocalDateTime.now())
                .commentsCount(1L)
                .build();
        testPost = postRepository.save(testPost);  // Сохраняем в базе данных

        postId = testPost.getId();  // Получаем id поста для дальнейшего использования

        // Создаем комментарий в базе данных
        testComment = commentRepository.save(Comment.builder()
                .commentType(CommentType.POST)
                .commentText("Content of test comment")
                .commentsCount(0)
                .isBlocked(false)
                .isDeleted(false)
                .time(LocalDateTime.now())
                .post(testPost)
                .build());

        commentId = testComment.getId();  // Получаем id комментария для дальнейшего использования

        // Создаем комментДто
        testCommentDto = CommentDto.builder()
                .id(commentId)
                .commentType(CommentType.POST)
                .commentText("Content of test comment")
                .commentsCount(0)
                .isBlocked(false)
                .isDeleted(false)
                .time(LocalDateTime.now())
                .postId(postId)
                .build();

        // Мокаем поведение валидации токена
        when(authServiceClient.validateToken(anyString())).thenReturn(true);

        doNothing().when(kafkaService).newCommentEvent(any(CommentNotificationDto.class));
    }

    @Test
    void getByPostId_shouldReturnComments() throws Exception {

        CommentSearchDto commentSearchDto = new CommentSearchDto();
        commentSearchDto.setIsDeleted(false);

        mockMvc.perform(get("/api/v1/post/{id}/comment", postId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentSearchDto))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.empty").value(false))
                .andReturn();
    }

    @Test
    void create_shouldCreateComment() throws Exception {

        mockMvc.perform(post("/api/v1/post/{id}/comment", postId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentDto)))
                .andExpect(status().isCreated());
    }


    @Test
    void update_shouldUpdateComment() throws Exception {

        mockMvc.perform(put("/api/v1/post/{id}/comment/{commentId}", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteById_shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/v1/post/{id}/comment/{commentId}", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf()))  // Добавляем CSRF-токен
                .andExpect(status().isNoContent());
    }

    @Test
    void addLikeToComment_shouldAddLike() throws Exception {
        mockMvc.perform(post("/api/v1/post/{id}/comment/{commentId}/like", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf()))  // Добавляем CSRF-токен
                .andExpect(status().isCreated());
    }

    @Test
    void removeLikeFromComment_shouldRemoveLike() throws Exception {

        testComment.setLikeAmount(1);
        commentRepository.save(testComment);

        reactionRepository.save(Reaction.builder()
                .commentId(commentId)
                .reactionType("No reaction")
                        .post(testPost)
                .type("COMMENT")
                .authorId(UUID.randomUUID())
                .build());

        mockMvc.perform(delete("/api/v1/post/{id}/comment/{commentId}/like", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf()))  // Добавляем CSRF-токен
                .andExpect(status().isNoContent());
    }

    @Test
    void getSubcomments_shouldReturnSubcomments() throws Exception {

        mockMvc.perform(get("/api/v1/post/{id}/comment/{commentId}/subcomment", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .param("page", "0")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldFailWhenCommentTextIsBlank() throws Exception {
        CommentDto invalidCommentDto = CommentDto.builder()
                .commentText("")  // Invalid comment text
                .build();

        mockMvc.perform(post("/api/v1/post/{id}/comment", postId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldFailWhenImagePathIsTooLong() throws Exception {
        String longImagePath = "a".repeat(513); // 513 characters, exceeding the limit
        CommentDto invalidCommentDto = CommentDto.builder()
                .imagePath(longImagePath)
                .build();

        mockMvc.perform(post("/api/v1/post/{id}/comment", postId)
                        .header(HttpHeaders.AUTHORIZATION, fakeToken)  // Добавляем фейковый токен
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());
    }
}