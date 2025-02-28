package ru.skillbox.social_network_post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.service.PostService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikeService likeService;


    @LogExecutionTime
    @GetMapping("/{id}")
    public PostDto getById(@PathVariable UUID id) {
        return postService.getById(id);
    }

    @LogExecutionTime
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @Valid @RequestBody PostDto postDto) {
        postService.update(id, postDto);
    }


    @LogExecutionTime
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        postService.delete(id);
    }


    @LogExecutionTime
    @GetMapping
    public PagePostDto getAll(
            @Valid @ModelAttribute PostSearchDto searchDto,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return postService.getAll(searchDto, pageable);
    }


    @LogExecutionTime
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody PostDto postDto) {
        postService.create(postDto);
    }


    @LogExecutionTime
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionDto addLikeToPost(@PathVariable UUID postId, @Valid @RequestBody ReactionDto reactionDto) {
        return likeService.addLikeToPost(postId, reactionDto);
    }


    @LogExecutionTime
    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromPost(@PathVariable UUID postId) {
        likeService.removeLikeFromPost(postId);
    }
}