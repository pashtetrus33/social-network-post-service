package ru.skillbox.social_network_post.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.web.model.PagePostDto;
import ru.skillbox.social_network_post.web.model.PostDto;
import ru.skillbox.social_network_post.web.model.PostSearchDto;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikeService likeService;


    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable Long id) {
        PostDto postDto = postService.getById(id);
        return ResponseEntity.ok(postDto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody PostDto postDto) {
        postService.update(id, postDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagePostDto> getAll(
            @Valid @ModelAttribute PostSearchDto searchDto,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(postService.getAll(searchDto, pageable));
    }



    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PostDto postDto, @RequestParam(required = false) Long publishDate) {
        postService.create(postDto, publishDate);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> addLikeToPost(@PathVariable Long postId) {
        likeService.addLikeToPost(postId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> removeLikeFromPost(@PathVariable Long postId) {
        likeService.removeLikeFromPost(postId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/storagePostPhoto")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String imagePath = postService.uploadPhoto(file);
        return ResponseEntity.ok(imagePath);
    }
}