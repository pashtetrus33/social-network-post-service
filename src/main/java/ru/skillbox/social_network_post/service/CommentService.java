package ru.skillbox.social_network_post.service;

import org.springframework.data.domain.Pageable;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;


public interface CommentService {

    PageCommentDto getByPostId(Long postId, Pageable pageable);

    void create(Long postId, CommentDto commentDto);

    void update(Long postId, Long commentId, CommentDto commentDto);

    void delete(Long postId, Long commentId);

    PageCommentDto getSubcomments(Long postId, Long commentId, Pageable pageable);
}