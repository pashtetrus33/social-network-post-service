package ru.skillbox.social_network_post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.aspect.LogMethodCall;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.service.ReactionService;
import ru.skillbox.social_network_post.service.PostService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ReactionService reactionService;


    @LogMethodCall
    @LogExecutionTime
    @GetMapping("/{id}")
    public PostDto getById(@PathVariable UUID id) {
        return postService.getById(id);
    }

    @LogMethodCall
    @LogExecutionTime
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody PostDto postDto) {
        postService.update(postDto);
    }


    @LogMethodCall
    @LogExecutionTime
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        postService.delete(id);
    }


    @LogMethodCall
    @LogExecutionTime
    @GetMapping
    public PagePostDto getAll(
            @Valid @ModelAttribute PostSearchDto postSearchDto,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return postService.getAll(postSearchDto, pageable);
    }


    @LogMethodCall
    @LogExecutionTime
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody PostDto postDto) {
        postService.create(postDto);
    }


    @LogMethodCall
    @LogExecutionTime
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionDto addLikeToPost(@PathVariable UUID postId, @Valid @RequestBody RequestReactionDto reactionDto) {
        return reactionService.addLikeToPost(postId, reactionDto);
    }


    @LogMethodCall
    @LogExecutionTime
    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromPost(@PathVariable UUID postId) {
        reactionService.removeLikeFromPost(postId);
    }
}