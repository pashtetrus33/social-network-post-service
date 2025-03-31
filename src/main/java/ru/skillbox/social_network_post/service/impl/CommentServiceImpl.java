package ru.skillbox.social_network_post.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.CommentType;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.mapper.CommentMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.specifiaction.CommentSpecification;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.ReactionService;
import ru.skillbox.social_network_post.utils.EntityCheckUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final ReactionService reactionService;
    private final KafkaService kafkaService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;


    @LogExecutionTime
    @Override
    @Transactional(readOnly = true)
    public PageCommentDto getByPostId(UUID postId, CommentSearchDto commentSearchDto, Pageable pageable) {

        commentSearchDto.setPostId(postId);
        commentSearchDto.setCommentType(CommentType.POST);

        // Формируем спецификацию для поиска
        Specification<Comment> spec = CommentSpecification.withFilters(commentSearchDto);

        log.warn("Service getByPostId spec: {}", spec);

        // Запрашиваем комменты из репозитория
        Page<Comment> comments = commentRepository.findAll(spec, pageable);

        PageCommentDto pageCommentDto = CommentMapperFactory.toPageCommentDto(comments);

        pageCommentDto.getContent().forEach(commentDto ->
                commentDto.setMyLike(reactionService.getMyReactionToComment(postId, commentDto.getId(), SecurityUtils.getAccountId())));

        return pageCommentDto;
    }


    @LogExecutionTime
    @Override
    @Transactional(readOnly = true)
    public PageCommentDto getSubcomments(UUID postId, UUID commentId, Pageable pageable) {
        EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkCommentPresence(commentRepository, commentId);
        Page<Comment> subcomments = commentRepository.findByParentCommentIdAndPostId(commentId, postId, pageable);

        PageCommentDto pageCommentDto = CommentMapperFactory.toPageCommentDto(subcomments);

        pageCommentDto.getContent().forEach(commentDto ->
                commentDto.setMyLike(reactionService.getMyReactionToComment(postId, commentDto.getId(), SecurityUtils.getAccountId())));

        return pageCommentDto;
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void create(UUID postId, CommentDto commentDto) {

        Post post = EntityCheckUtils.checkPostPresence(postRepository, postId);

        if (commentDto.getTime() == null) {
            commentDto.setTime(LocalDateTime.now(ZoneOffset.UTC));
        }

        Comment comment = CommentMapperFactory.toComment(commentDto);

        UUID parentId = commentDto.getParentId();

        if (parentId != null) {
            comment.setParentComment(EntityCheckUtils.checkCommentPresence(commentRepository, parentId));
            comment.setCommentType(CommentType.COMMENT);
            commentRepository.incrementCommentsAmount(parentId);
        } else {
            comment.setCommentType(CommentType.POST);

        }

        comment.setPost(post);
        comment.setId(null); // Сбрасываем ID, чтобы Hibernate сгенерировал новый
        comment.setIsBlocked(false);
        comment.setIsDeleted(false);
        comment.setLikeAmount(0);

        UUID accountId = SecurityUtils.getAccountId();

        comment.setAuthorId(accountId);

        post.setCommentsCount(post.getCommentsCount() + 1);

        Comment savedComment = commentRepository.save(comment);

        CommentNotificationDto commentNotificationDto = CommentNotificationDto.builder()
                .authorId(accountId)
                .commentId(savedComment.getId())
                .parentId(parentId)
                .postId(postId)
                .shortCommentText(StringUtils.abbreviate(comment.getCommentText(), 80))
                .publishDate(comment.getTime())
                .build();


        kafkaService.newCommentEvent(commentNotificationDto);
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void update(UUID postId, UUID commentId, CommentDto commentDto) {

        if (!commentId.equals(commentDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", commentDto.getId(), commentId));
        }

        if (commentDto.getPostId() == null) {
            commentDto.setPostId(postId);
        }

        // Получаем пост и комментарий одной операцией
        Pair<Post, Comment> pair = EntityCheckUtils.checkCommentAndPostPresence(commentRepository, postRepository, postId, commentId);
        Post post = pair.getLeft();
        Comment comment = pair.getRight();

        // Обновляем текст, время и другие поля
        CommentMapperFactory.updateCommentFromDto(commentDto, comment);

        // Обновляем parentComment, если поменялся
        updateParentComment(commentDto, comment);

        // Проверка: менять пост нельзя!
        if (!Objects.equals(commentDto.getPostId(), post.getId())) {
            throw new ValidationException("Cannot change post for an existing comment");
        }

        // Всегда обновляем время
        comment.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));

        commentRepository.save(comment);
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void delete(UUID postId, UUID commentId) {

        Pair<Post, Comment> postCommentPair = EntityCheckUtils.checkCommentAndPostPresence(commentRepository, postRepository, postId, commentId);
        Comment comment = postCommentPair.getRight();

        // Помечаем комментарии как удаленные
        commentRepository.markCommentAsDeletedByPostIdAndCommentId(postId, commentId);

        if (comment.getCommentsCount() > 0) {
            commentRepository.markAllAsDeletedByParentComment(commentId);
        }

        postRepository.decrementCommentCount(postId, comment.getCommentsCount() + 1);
    }

    private void updateParentComment(CommentDto commentDto, Comment comment) {
        UUID newParentId = commentDto.getParentId();
        UUID existingParentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;

        if (!Objects.equals(newParentId, existingParentId)) {
            if (newParentId != null) {
                comment.setParentComment(EntityCheckUtils.checkCommentPresence(commentRepository, newParentId));
            } else {
                comment.setParentComment(null);
            }
        }
    }
}