package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

    private final KafkaService kafkaService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;


    @LogExecutionTime
    @Override
    //@Cacheable(value = "comments", key = "#postId") // Кэшируем комменты для поста
    @Transactional(readOnly = true)
    public PageCommentDto getByPostId(UUID postId, CommentSearchDto commentSearchDto, Pageable pageable) {

        log.info("Service getByPostId: {}", postId);

        commentSearchDto.setPostId(postId);
        commentSearchDto.setCommentType(CommentType.POST);

        log.info("Service getByPostId commentsearchdto: {}", commentSearchDto);

        // Формируем спецификацию для поиска
        Specification<Comment> spec = CommentSpecification.withFilters(commentSearchDto);

        log.info("Service getByPostId spec: {}", spec);

        // Запрашиваем комменты из репозитория
        Page<Comment> comments = commentRepository.findAll(spec, pageable);

        for (Comment comment : comments) {
            log.info("!!Service getByPostId comment: {}", comment);
        }

        return CommentMapperFactory.toPageCommentDto(comments);
    }


    @LogExecutionTime
    @Override
    @Transactional(readOnly = true)
    public PageCommentDto getSubcomments(UUID postId, UUID commentId, Pageable pageable) {
        EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkCommentPresence(commentRepository, commentId);
        Page<Comment> subcomments = commentRepository.findByParentCommentIdAndPostId(commentId, postId, pageable);
        return CommentMapperFactory.toPageCommentDto(subcomments);
    }


    //    @Caching(evict = {
//            @CacheEvict(value = "posts", key = "#postId"), // Очистка кэша поста
//            @CacheEvict(value = "post_pages", allEntries = true), // Очистка всех страниц постов
//            @CacheEvict(value = "comments", key = "#postId") // Очистка кэша комментариев для поста
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void create(UUID postId, CommentDto commentDto) {

        Post post = EntityCheckUtils.checkPostPresence(postRepository, postId);

        Comment comment = CommentMapperFactory.toComment(commentDto);

        UUID parentId = commentDto.getParentId();

        if (parentId != null) {
            comment.setParentComment(EntityCheckUtils.checkCommentPresence(commentRepository, parentId));
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

        UUID accountId = SecurityUtils.getAccountId();

        comment.setAuthorId(accountId);

        post.setCommentsCount(post.getCommentsCount() + 1);

        if (commentDto.getTime() == null) {
            comment.setTime(LocalDateTime.now(ZoneOffset.UTC));
        }

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


    //    @Caching(evict = {
//            @CacheEvict(value = "posts", key = "#postId"),
//            @CacheEvict(value = "post_pages", allEntries = true),
//            @CacheEvict(value = "comments", key = "#postId")
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void update(UUID postId, UUID commentId, CommentDto commentDto) {

        if (!commentId.equals(commentDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", commentDto.getId(), commentId));
        }

        EntityCheckUtils.checkCommentAndPostPresence(commentRepository, postRepository, postId, commentId);

        Comment comment = EntityCheckUtils.checkCommentPresence(commentRepository, commentId);

        CommentMapperFactory.updateCommentFromDto(commentDto, comment);

        if (!Objects.equals(commentDto.getParentId(), comment.getParentComment().getId())) {
            comment.setParentComment(EntityCheckUtils.checkCommentPresence(commentRepository, commentDto.getParentId()));
        }

        if (!Objects.equals(commentDto.getPostId(), comment.getPost().getId())) {
            comment.setPost(EntityCheckUtils.checkPostPresence(postRepository, commentDto.getPostId()));
        }

        if (commentDto.getTimeChanged() == null) {
            comment.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));
        }

        commentRepository.save(comment);
    }


    //    @Caching(evict = {
//            @CacheEvict(value = "posts", key = "#postId"),
//            @CacheEvict(value = "post_pages", allEntries = true),
//            @CacheEvict(value = "comments", key = "#postId")
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void delete(UUID postId, UUID commentId) {

        EntityCheckUtils.checkCommentAndPostPresence(commentRepository, postRepository, postId, commentId);
        // Помечаем комментарии как удаленные
        commentRepository.markCommentAsDeletedByPostIdAndCommentId(postId, commentId);

        postRepository.decrementCommentCount(postId);
    }
}