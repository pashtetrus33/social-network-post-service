package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.CommentType;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.mapper.CommentMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.dto.CommentDto;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.dto.PageCommentDto;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final KafkaService kafkaService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;


    @Override
    @Cacheable(value = "comments", key = "#postId")
    @Transactional(readOnly = true)
    public PageCommentDto getByPostId(UUID postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        log.info("Fetched {} comments for post ID {}", comments.getTotalElements(), postId);
        return CommentMapperFactory.toPageCommentDto(comments);
    }


    @Override
    @Cacheable(value = "subcomments", key = "#commentId")
    @Transactional(readOnly = true)
    public PageCommentDto getSubcomments(UUID postId, UUID commentId, Pageable pageable) {
        checkPostPresence(postId);
        checkCommentPresence(commentId);
        Page<Comment> subcomments = commentRepository.findByParentCommentIdAndPostId(commentId, postId, pageable);
        log.info("Fetched {} subcomments for comment ID {}", subcomments.getTotalElements(), commentId);
        return CommentMapperFactory.toPageCommentDto(subcomments);
    }


    @Override
    @CacheEvict(value = "comments", key = "#postId")
    @Transactional
    public void create(UUID postId, CommentDto commentDto) {

        Post post = checkPostPresence(postId);

        Comment comment = CommentMapperFactory.toComment(commentDto);

        UUID parentId = commentDto.getParentId();

        if (parentId != null) {
            comment.setParentComment(checkCommentPresence(parentId));
            comment.setCommentType(CommentType.COMMENT);
        } else {
            comment.setCommentType(CommentType.POST);
        }

        comment.setPost(post);

        comment.setId(null); // Сбрасываем ID, чтобы Hibernate сгенерировал новый
        comment.setIsBlocked(false);
        comment.setIsDeleted(false);
        comment.setLikeAmount(0);
        comment.setCommentsCount(0);
        comment.setMyLike(false);

        UUID accountId = getAccountId();

        comment.setAuthorId(accountId);

        post.setCommentsCount(post.getCommentsCount() + 1);

        if (commentDto.getTime() == null) {
            post.setTime(LocalDateTime.now(ZoneOffset.UTC));
        }

        commentRepository.save(comment);

        if (comment.getCommentType().equals(CommentType.POST)) {
            kafkaService.newCommentToPostEvent(new KafkaDto(accountId, postId));
        }

        if (comment.getCommentType().equals(CommentType.COMMENT)) {
            kafkaService.newCommentToCommentEvent(new KafkaDto(accountId, parentId));
        }
    }


    @Override
    @CacheEvict(value = "comments", key = "#postId")
    @Transactional
    public void update(UUID postId, UUID commentId, CommentDto commentDto) {

        if (!commentId.equals(commentDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", commentDto.getId(), commentId));
        }

        checkCommentAndPostPresence(postId, commentId);

        Comment comment = checkCommentPresence(commentId);

        CommentMapperFactory.updateCommentFromDto(commentDto, comment);

        if (!Objects.equals(commentDto.getParentId(), comment.getParentComment().getId())) {
            comment.setParentComment(checkCommentPresence(commentDto.getParentId()));
        }

        if (!Objects.equals(commentDto.getPostId(), comment.getPost().getId())) {
            comment.setPost(checkPostPresence(commentDto.getPostId()));
        }

        if (commentDto.getTimeChanged() == null) {
            comment.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));
        }

        commentRepository.save(comment);

        log.info("Updated comment with ID {} for post ID {}", commentId, postId);
    }


    @Override
    @CacheEvict(value = "comments", key = "#postId")
    @Transactional
    public void delete(UUID postId, UUID commentId) {

        checkCommentAndPostPresence(postId, commentId);

        // Помечаем комментарии как удаленные
        commentRepository.markAllAsDeletedByPostId(postId);
        log.info("Marked as deleted comment with ID {} and all its children for post ID {}", commentId, postId);
    }


    private Post checkPostPresence(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new EntityNotFoundException(MessageFormat.format("Post with id {0} not found", postId));
                });
    }


    private Comment checkCommentPresence(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment with ID {} not found", commentId);
                    return new EntityNotFoundException(MessageFormat.format("Comment with id {0} not found", commentId));
                });
    }


    private void checkCommentAndPostPresence(UUID postId, UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            log.warn("Comment with ID {} not found", commentId);
            throw new EntityNotFoundException("Comment with ID " + commentId + " not found");
        }

        if (!postRepository.existsById(postId)) {
            log.warn("Post with ID {} not found", postId);
            throw new EntityNotFoundException("Post with ID " + postId + " not found");
        }
    }

    private static UUID getAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UUID) authentication.getPrincipal());
    }
}