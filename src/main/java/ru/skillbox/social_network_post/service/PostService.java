package ru.skillbox.social_network_post.service;


import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostSearchDto;
import ru.skillbox.social_network_post.entity.Post;

import java.util.List;
import java.util.UUID;

public interface PostService {

    PostDto getById(UUID postId);

    void update(UUID postId, PostDto postDto);

    void delete(UUID postId);

    PagePostDto getAll(PostSearchDto searchDto, Pageable pageable);

    void create(PostDto postDto, Long publishDate);

    void updateBlockedStatusForAccount(UUID uuid);

    void updateDeletedStatusForAccount(UUID uuid);
}