package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.CommentNotFoundException;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.exception.PostNotFoundException;
import ru.skillbox.social_network_post.mapper.CommentMapper;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.KafkaDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

import java.text.MessageFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final KafkaService kafkaService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;


    @Override
    @Cacheable(value = "comments", key = "#postId")
    @Transactional
    public PageCommentDto getByPostId(Long postId, Pageable pageable) {
        checkPostPresence(postId);
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        log.info("Fetched {} comments for post ID {}", comments.getTotalElements(), postId);
        return commentMapper.toPageCommentDto(comments);
    }

    @Override
    @CacheEvict(value = "comments", key = "#postId")
    @Transactional
    public void create(Long postId, CommentDto commentDto) {

        Post post = checkPostPresence(postId);

        Comment comment = commentMapper.toComment(commentDto);
        comment.setPost(post);

        comment.setId(null); // Сбрасываем ID, чтобы Hibernate сгенерировал новый

        post.setCommentsCount(post.getCommentsCount() + 1);

        commentRepository.save(comment);


        log.info("Created comment with ID {} for post ID {}", comment.getId(), postId);

        KafkaDto kafkaDto = new KafkaDto(MessageFormat.format("Comment created for post ID {0}", postId));

        kafkaService.produce(kafkaDto);
    }

    @Override
    @CacheEvict(value = "comments", key = "#postId")
    @Transactional
    public void update(Long postId, Long commentId, CommentDto commentDto) {

        if (!commentId.equals(commentDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", commentDto.getId(), commentId));
        }

        Comment comment = checkCommentAndPostPresence(postId, commentId);

        commentMapper.updateCommentFromDto(commentDto, comment);
        commentRepository.save(comment);
        log.info("Updated comment with ID {} for post ID {}", commentId, postId);
    }


    @Override
    @CacheEvict(value = "comments", key = "#postId")
    @Transactional
    public void delete(Long postId, Long commentId) {
        commentRepository.delete(checkCommentPresence(commentId));
        log.info("Deleted comment with ID {} for post ID {}", commentId, postId);
    }

    @Override
    @Cacheable(value = "subcomments", key = "#commentId")
    @Transactional
    public PageCommentDto getSubcomments(Long postId, Long commentId, Pageable pageable) {
        checkPostPresence(postId);
        checkCommentPresence(commentId);
        Page<Comment> subcomments = commentRepository.findByParentCommentIdAndPostId(commentId, postId, pageable);
        log.info("Fetched {} subcomments for comment ID {}", subcomments.getTotalElements(), commentId);
        return commentMapper.toPageCommentDto(subcomments);
    }


    private Post checkPostPresence(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new PostNotFoundException(MessageFormat.format("Post with id {0} not found", postId));
                });
    }

    private Comment checkCommentPresence(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment with ID {} not found", commentId);
                    return new CommentNotFoundException(MessageFormat.format("Comment with id {0} not found", commentId));
                });
    }

    private Comment checkCommentAndPostPresence(Long postId, Long commentId) {

        checkPostPresence(postId);
        Comment comment = checkCommentPresence(commentId);

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalStateException(MessageFormat.format(
                    "Comment with id {0} does not belong to post with id {1}", commentId, postId));
        }

        return comment;
    }
}