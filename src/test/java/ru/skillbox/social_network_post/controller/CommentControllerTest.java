package ru.skillbox.social_network_post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skillbox.social_network_post.dto.CommentDto;
import ru.skillbox.social_network_post.dto.PageCommentDto;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.ReactionService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "USER")
@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;  // Mock the service

    @MockBean
    private ReactionService reactionService; // Mock the service

    private UUID postId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();
    }

    @Test
    void getByPostId_shouldReturnComments() throws Exception {

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("id")));

        PageCommentDto pageCommentDto = PageCommentDto.builder()
                .totalElements(5L)
                .totalPages(1)
                .number(0)
                .size(10)
                .content(List.of(CommentDto.builder()
                        .commentText("Test comment")
                        .build()))
                .first(true)
                .last(true)
                .numberOfElements(1)
                .empty(false)
                .build();

        when(commentService.getByPostId(any(UUID.class), eq(pageable))).thenReturn(pageCommentDto);

        mockMvc.perform(get("/api/v1/post/{id}/comment", postId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())  // Expect 'content' to be an array
                .andExpect(jsonPath("$.content.length()").value(1))  // Expect 1 element in the 'content' array
                .andExpect(jsonPath("$.totalElements").value(5))
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
        CommentDto commentDto = CommentDto.builder()
                .commentText("Test comment")
                .build();

        mockMvc.perform(post("/api/v1/post/{id}/comment", postId)
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated());

        verify(commentService, times(1)).create(postId, commentDto);
    }


    @Test
    void update_shouldUpdateComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .commentText("Updated comment")
                .build();

        mockMvc.perform(put("/api/v1/post/{id}/comment/{commentId}", postId, commentId)
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).update(postId, commentId, commentDto);
    }

    @Test
    void deleteById_shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/v1/post/{id}/comment/{commentId}", postId, commentId)
                        .with(csrf()))  // Добавляем CSRF-токен
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).delete(postId, commentId);
    }

    @Test
    void addLikeToComment_shouldAddLike() throws Exception {
        mockMvc.perform(post("/api/v1/post/{id}/comment/{commentId}/like", postId, commentId)
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(reactionService, times(1)).addLikeToComment(postId, commentId);
    }

    @Test
    void removeLikeFromComment_shouldRemoveLike() throws Exception {
        mockMvc.perform(delete("/api/v1/post/{id}/comment/{commentId}/like", postId, commentId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reactionService, times(1)).removeLikeFromComment(postId, commentId);
    }

    @Test
    void getSubcomments_shouldReturnSubcomments() throws Exception {
        PageCommentDto pageCommentDto = new PageCommentDto(); // Populate this with test data
        when(commentService.getSubcomments(postId, commentId, Pageable.unpaged())).thenReturn(pageCommentDto);

        mockMvc.perform(get("/api/v1/post/{id}/comment/{commentId}/subcomment", postId, commentId)
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
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());
    }
}