package ru.skillbox.social_network_post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostSearchDto;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.service.ReactionService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "USER")
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private ReactionService reactionService;

    private UUID postId;
    private PostDto postDto;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        postDto = PostDto.builder()
                .id(postId)
                .title("title")
                .postText("Test post text")
                .build();
    }


    @Test
    void getById_shouldReturnPost() throws Exception {
        when(postService.getById(postId)).thenReturn(postDto);

        mockMvc.perform(get("/api/v1/post/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.postText").value("Test post text"));

        verify(postService, times(1)).getById(postId);
    }


    @Test
    void create_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/post")
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated());

        verify(postService, times(1)).create(postDto);
    }


    @Test
    void update_shouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/v1/post/{id}", postId)
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isNoContent());

        verify(postService).update(eq(postId), any(PostDto.class));

        verify(postService, times(1)).update(postId, postDto);
    }


    @Test
    void deleteById_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/post/{id}", postId)
                        .with(csrf())) // Добавляем CSRF-токен
                .andExpect(status().isNoContent());

        verify(postService, times(1)).delete(postId);
    }


    @Test
    void getAll_shouldReturnPagePostDto() throws Exception {
        when(postService.getAll(any(PostSearchDto.class), any()))
                .thenReturn(new PagePostDto());

        mockMvc.perform(get("/api/v1/post"))
                .andExpect(status().isOk());

        verify(postService, times(1)).getAll(any(PostSearchDto.class), any());
    }


    @Test
    void addLikeToPost_ShouldReturnCreated() throws Exception {
        UUID postId = UUID.randomUUID();
        ReactionDto reactionDto = ReactionDto.builder()
                .type("LIKE")
                .reactionType("POSITIVE")
                .count(1L)
                .build();
        when(reactionService.addLikeToPost(eq(postId), any())).thenReturn(reactionDto);

        mockMvc.perform(post("/api/v1/post/" + postId + "/like")
                        .with(csrf())  // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactionDto))) // Используем ObjectMapper
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("LIKE"))
                .andExpect(jsonPath("$.reactionType").value("POSITIVE"))
                .andExpect(jsonPath("$.count").value(1));
    }


    @Test
    void removeLikeFromPost_ShouldReturnNoContent() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/post/" + postId + "/like")
                        .with(csrf())) // Добавляем CSRF-токен
                .andExpect(status().isNoContent());

        verify(reactionService, times(1)).removeLikeFromPost(postId);
    }
}