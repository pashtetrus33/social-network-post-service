package ru.skillbox.social_network_post.service;

import org.springframework.data.domain.Pageable;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.SearchDto;

import java.util.UUID;

public interface PostService {

    PostDto getById(UUID postId);

    void update(PostDto postDto);

    void delete(UUID postId);

    PagePostDto getAll(SearchDto searchDto, Pageable pageable);

    void create(PostDto postDto);

    void updateBlockedStatusForAccount(UUID uuid);

    void updateDeletedStatusForAccount(UUID uuid);
}