package ru.skillbox.social_network_post.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post/{id}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;


    @GetMapping
    public ResponseEntity<PageCommentDto> getByPostId(@PathVariable UUID id,
                                                      @RequestParam int page,
                                                      @RequestParam int size,
                                                      @RequestParam(defaultValue = "id,asc") String[] sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(commentService.getByPostId(id, pageable));
    }


    @PostMapping
    public ResponseEntity<Void> create(@PathVariable UUID id, @Valid @RequestBody CommentDto commentDto) {
        commentService.create(id, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @PathVariable UUID commentId, @Valid @RequestBody CommentDto commentDto) {
        commentService.update(id, commentId, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id, @PathVariable UUID commentId) {
        commentService.delete(id, commentId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> addLikeToComment(@PathVariable UUID id, @PathVariable UUID commentId) {
        likeService.addLikeToComment(id, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Void> removeLikeFromComment(@PathVariable UUID id, @PathVariable UUID commentId) {
        likeService.removeLikeFromComment(id, commentId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{commentId}/subcomment")
    public ResponseEntity<PageCommentDto> getSubcomments(@PathVariable UUID id, @PathVariable UUID commentId,
                                                         @RequestParam int page,
                                                         @RequestParam int size,
                                                         @RequestParam(defaultValue = "id,asc") String[] sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        PageCommentDto subcommentDtos = commentService.getSubcomments(id, commentId, pageable);
        return ResponseEntity.ok(subcommentDtos);
    }
}