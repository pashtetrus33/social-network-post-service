package ru.skillbox.social_network_post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.dto.LikeDto;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostSearchDto;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void addLikeToPost(@PathVariable UUID postId, @Valid @RequestBody LikeDto likeDto) {
        likeService.addLikeToPost(postId, likeDto);
    }


    @LogExecutionTime
    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromPost(@PathVariable UUID postId) {
        likeService.removeLikeFromPost(postId);
    }
}