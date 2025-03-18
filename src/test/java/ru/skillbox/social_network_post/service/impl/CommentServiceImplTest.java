package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.IdMismatchException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentServiceImplTest extends AbstractServiceTest {

    @BeforeEach
    void setUp() {
        super.setUp(); // Вызов метода из абстрактного класса для инициализации аутентификации и очистки данных.
    }

//    @Test
//    void testCreateComment() {
//        UUID postId = UUID.randomUUID();
//        CommentDto commentDto = new CommentDto();
//        commentDto.setCommentText("Test Comment");
//        commentDto.setTime(LocalDateTime.now());
//
//        // Arrange
//        Post post = Post.builder()
//                .title("New Test Post")
//                .postText("This is a new test post")
//                .publishDate(LocalDateTime.now())
//                .build();
//
//        when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Post()));
//
//        // Мокируем commentRepository.save
//        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());
//
//        // Мокируем вызов Kafka
//        doNothing().when(kafkaService).newCommentEvent(any());
//
//        commentService.create(postId, commentDto);
//
//        // Проверяем, что комментарий был сохранен
//        verify(commentRepository, times(1)).save(any(Comment.class));
//        assertEquals(1, post.getCommentsCount());  // Количество комментариев в посте должно увеличиться
//
//        // Проверяем, что Kafka сервис был вызван
//        verify(kafkaService, times(1)).newCommentEvent(any());
//    }
//
//    @Test
//    void testGetByPostId() {
//        UUID postId = UUID.randomUUID();
//        CommentSearchDto commentSearchDto = new CommentSearchDto();
//        Pageable pageable = PageRequest.of(0, 10);
//
//        Comment comment = new Comment();
//        comment.setCommentText("Test Comment");
//        comment.setId(UUID.randomUUID());
//        comment.setPost(new Post());
//        comment.setTime(LocalDateTime.now());
//
//        Page<Comment> commentsPage = mock(Page.class);
//        when(commentRepository.findAll((Example<Comment>) any(), eq(pageable))).thenReturn(commentsPage);
//
//        PageCommentDto pageCommentDto = commentService.getByPostId(postId, commentSearchDto, pageable);
//
//        assertNotNull(pageCommentDto);
//        verify(commentRepository, times(1)).findAll((Example<Comment>) any(), eq(pageable));
//    }
//
//    @Test
//    void testGetSubcomments() {
//        UUID postId = UUID.randomUUID();
//        UUID commentId = UUID.randomUUID();
//        Pageable pageable = PageRequest.of(0, 10);
//
//        Comment comment = new Comment();
//        comment.setCommentText("Test Subcomment");
//        comment.setId(UUID.randomUUID());
//        comment.setPost(new Post());
//
//        Page<Comment> subcommentsPage = mock(Page.class);
//        when(commentRepository.findByParentCommentIdAndPostId(eq(commentId), eq(postId), eq(pageable))).thenReturn(subcommentsPage);
//
//        PageCommentDto pageCommentDto = commentService.getSubcomments(postId, commentId, pageable);
//
//        assertNotNull(pageCommentDto);
//        verify(commentRepository, times(1)).findByParentCommentIdAndPostId(eq(commentId), eq(postId), eq(pageable));
//    }
//
//    @Test
//    void testUpdateComment() {
//        UUID postId = UUID.randomUUID();
//        UUID commentId = UUID.randomUUID();
//        CommentDto commentDto = new CommentDto();
//        commentDto.setId(commentId);
//        commentDto.setCommentText("Updated Comment");
//
//        Comment existingComment = new Comment();
//        existingComment.setId(commentId);
//        existingComment.setPost(new Post());
//        existingComment.setCommentText("Old Comment");
//
//        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(existingComment));
//        when(postRepository.findById(postId)).thenReturn(java.util.Optional.of(new Post()));
//
//        commentService.update(postId, commentId, commentDto);
//
//        // Проверяем, что комментарий был обновлен
//        verify(commentRepository, times(1)).save(existingComment);
//        assertEquals("Updated Comment", existingComment.getCommentText());
//    }
//
//    @Test
//    void testUpdateCommentIdMismatch() {
//        UUID postId = UUID.randomUUID();
//        UUID commentId = UUID.randomUUID();
//        CommentDto commentDto = new CommentDto();
//        commentDto.setId(UUID.randomUUID());  // Другой ID для комментария
//
//        assertThrows(IdMismatchException.class, () -> {
//            commentService.update(postId, commentId, commentDto);
//        });
//    }

//    @Test
//    void testDeleteComment() {
//        UUID postId = UUID.randomUUID();
//        UUID commentId = UUID.randomUUID();
//
//        when(postRepository.findById(postId)).thenReturn(java.util.Optional.of(new Post()));
//        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(new Comment()));
//
//        commentService.delete(postId, commentId);
//
//        // Проверяем, что комментарий был помечен как удаленный
//        verify(commentRepository, times(1)).markCommentAsDeletedByPostIdAndCommentId(postId, commentId);
//        verify(postRepository, times(1)).decrementCommentCount(postId);
//    }
}