package ru.skillbox.social_network_post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.ReactionService;
import ru.skillbox.social_network_post.dto.CommentDto;
import ru.skillbox.social_network_post.dto.PageCommentDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post/{id}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ReactionService reactionService;


    @LogExecutionTime
    @GetMapping
    public PageCommentDto getByPostId(
            @PathVariable UUID id,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return commentService.getByPostId(id, pageable);
    }


    @LogExecutionTime
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable UUID id, @Valid @RequestBody CommentDto commentDto) {
        commentService.create(id, commentDto);
    }


    @LogExecutionTime
    @PutMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @PathVariable UUID commentId, @Valid @RequestBody CommentDto commentDto) {
        commentService.update(id, commentId, commentDto);
    }


    @LogExecutionTime
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id, @PathVariable UUID commentId) {
        commentService.delete(id, commentId);
    }


    @LogExecutionTime
    @PostMapping("/{commentId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public void addLikeToComment(@PathVariable UUID id, @PathVariable UUID commentId) {
        reactionService.addLikeToComment(id, commentId);
    }


    @LogExecutionTime
    @DeleteMapping("/{commentId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromComment(@PathVariable UUID id, @PathVariable UUID commentId) {
        reactionService.removeLikeFromComment(id, commentId);
    }


    @LogExecutionTime
    @GetMapping("/{commentId}/subcomment")
    public PageCommentDto getSubcomments(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return commentService.getSubcomments(id, commentId, pageable);
    }
}