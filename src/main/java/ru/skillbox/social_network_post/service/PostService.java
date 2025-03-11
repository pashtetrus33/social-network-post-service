package ru.skillbox.social_network_post.service;

import org.springframework.data.domain.Pageable;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostSearchDto;

import java.util.UUID;

public interface PostService {

    PostDto getById(UUID postId);

    void update(PostDto postDto);

    void delete(UUID postId);

    PagePostDto getAll(PostSearchDto postSearchDto, Pageable pageable);

    void create(PostDto postDto);

    void updateBlockedStatusForAccount(UUID uuid);

    void updateDeletedStatusForAccount(UUID uuid);
}