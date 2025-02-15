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


    @GetMapping("/{id}")
    public PostDto getById(@PathVariable UUID id) {
        return postService.getById(id);
    }


    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @Valid @RequestBody PostDto postDto) {
        postService.update(id, postDto);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        postService.delete(id);
    }


    @GetMapping
    public PagePostDto getAll(
            @Valid @ModelAttribute PostSearchDto searchDto,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Principal principal) {

        System.out.println("Username: " + principal.getName());

        // Получаем Authentication из SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("UserId: " + authentication.getPrincipal());


        // Извлекаем роли
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        System.out.println("Roles: " + roles);

        return postService.getAll(searchDto, pageable);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody PostDto postDto, @RequestParam(required = false) Long publishDate) {
        postService.create(postDto, publishDate);
    }


    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public void addLikeToPost(@PathVariable UUID postId) {
        likeService.addLikeToPost(postId);
    }


    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromPost(@PathVariable UUID postId) {
        likeService.removeLikeFromPost(postId);
    }


    @PostMapping("/storagePostPhoto")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return postService.uploadPhoto(file);
    }
}