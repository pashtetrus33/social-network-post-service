package ru.skillbox.social_network_post.service;

import org.springframework.data.domain.Pageable;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

import java.util.UUID;


public interface CommentService {

    PageCommentDto getByPostId(UUID postId, Pageable pageable);

    void create(UUID postId, CommentDto commentDto);

    void update(UUID postId, UUID commentId, CommentDto commentDto);

    void delete(UUID postID, UUID commentId);

    PageCommentDto getSubcomments(UUID postId, UUID commentId, Pageable pageable);
}