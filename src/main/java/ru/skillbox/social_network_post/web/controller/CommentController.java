package ru.skillbox.social_network_post.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

@RestController
@RequestMapping("/api/v1/post/{id}/comment")
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    public CommentController(CommentService commentService, LikeService likeService) {
        this.commentService = commentService;
        this.likeService = likeService;
    }

    // Получить комментарии по ID поста
    @GetMapping
    public ResponseEntity<PageCommentDto> getByPostId(@PathVariable Long id, @RequestParam Pageable pageable) {
        return ResponseEntity.ok(commentService.getByPostId(id, pageable));
    }

    // Создать новый комментарий
    @PostMapping
    public ResponseEntity<Void> create(@PathVariable Long id, @Valid @RequestBody CommentDto commentDto) {
        commentService.create(id, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Обновить комментарий
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> update(@PathVariable Long id, @PathVariable Long commentId, @Valid @RequestBody CommentDto commentDto) {
        commentService.update(id, commentId, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Удалить комментарий
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id, @PathVariable Long commentId) {
        commentService.delete(id, commentId);
        return ResponseEntity.ok().build();
    }

    // Добавить лайк к комментарию
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> addLikeToComment(@PathVariable Long id, @PathVariable Long commentId) {
        likeService.addLikeToComment(id, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Удалить лайк с комментария
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Void> removeLikeFromComment(@PathVariable Long id, @PathVariable Long commentId) {
        likeService.removeLikeFromComment(id, commentId);
        return ResponseEntity.noContent().build();
    }

    // Получить подкомментарии
    @GetMapping("/{commentId}/subcomment")
    public ResponseEntity<PageCommentDto> getSubcomments(@PathVariable Long id, @PathVariable Long commentId, @RequestParam Pageable pageable) {
        PageCommentDto subcommentDtos = commentService.getSubcomments(id, commentId, pageable);
        return ResponseEntity.ok(subcommentDtos);
    }
}