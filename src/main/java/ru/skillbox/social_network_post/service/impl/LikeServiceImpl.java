package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.dto.LikeDto;
import ru.skillbox.social_network_post.entity.Like;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.mapper.LikeMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.LikeRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.LikeService;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private UUID accountId;
    private final KafkaService kafkaService;

    @Override
    @Transactional
    public void addLikeToPost(UUID postId, LikeDto likeDto) {

        log.info("Adding like to post with id: {}", postId);

        checkPostPresence(postId);

        checkLikeDto(likeDto);

        accountId = getAccountId();

        // Проверяем, ставил ли пользователь лайк ранее
        if (likeRepository.existsByPostIdAndAuthorId(postId, accountId)) {
            throw new IllegalStateException(
                    MessageFormat.format("Like already exists for post with id {0}", postId));
        }

        Like like = LikeMapperFactory.toLike(likeDto);
        like.setPostId(postId);
        like.setAuthorId(accountId);
        likeRepository.save(like);

        // Проверяем, является ли текущий пользователь автором поста
        if (postRepository.isAuthorOfPost(postId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            postRepository.incrementLikeAmountAndSetMyLike(postId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            postRepository.incrementLikeAmount(postId);
        }

        kafkaService.newLikeToPostEvent(new KafkaDto(accountId, postId));
    }


    @Override
    @Transactional
    public void removeLikeFromPost(UUID postId) {

        log.info("Removing like from post with id: {}", postId);

        checkPostPresence(postId);

        int likeAmount = postRepository.getLikeAmount(postId);

        if (likeAmount <= 0) {
            throw new IllegalStateException(
                    MessageFormat.format("Невозможно удалить лайк с поста {0}: количество лайков уже 0", postId));
        }

        accountId = getAccountId();

        // Проверяем, является ли текущий пользователь автором поста
        if (postRepository.isAuthorOfPost(postId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            postRepository.updateLikeAmountAndUnsetMyLike(postId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            postRepository.updateLikeAmount(postId);
        }
    }


    @Override
    @Transactional
    public void addLikeToComment(UUID postId, UUID commentId) {

        log.info("Adding like to comment with id: {} on post id: {}", commentId, postId);

        checkPostPresence(postId);
        checkCommentPresence(commentId);

        accountId = getAccountId();

        // Проверка, существует ли комментарий с таким ID и связан ли он с постом
        if (!commentRepository.existsByPostIdAndId(postId, commentId)) {
            throw new EntityNotFoundException(
                    MessageFormat.format("Comment with id {0} not found for post with id {1}", commentId, postId));
        }

        // Проверяем, ставил ли пользователь лайк для данного комментария
        if (likeRepository.existsByCommentIdAndAuthorId(commentId, accountId)) {
            log.warn("Like already exists for post with id {} and comment with id {}", postId, commentId);
            throw new IllegalStateException(
                    MessageFormat.format("Like already exists for post with id {0} and comment with id {1}", postId, commentId));
        }


        // Проверяем, является ли текущий пользователь автором коме комментария
        if (commentRepository.isAuthorOfComment(commentId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            commentRepository.incrementLikeAmountAndSetMyLike(commentId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            commentRepository.incrementLikeAmount(commentId);
        }

        kafkaService.newLikeToCommentEvent(new KafkaDto(accountId, commentId));
    }


    @Override
    @Transactional
    public void removeLikeFromComment(UUID postId, UUID commentId) {

        log.info("Removing like from comment with id: {} on post id: {}", commentId, postId);

        checkPostPresence(postId);
        checkCommentPresence(commentId);

        int likeAmount = commentRepository.getLikeAmount(commentId);

        if (likeAmount <= 0) {
            throw new IllegalStateException(
                    MessageFormat.format("Cannot remove like from post {0} and comment {1}: like count is already 0", postId, commentId));
        }

        accountId = getAccountId();

        // Проверяем, является ли текущий пользователь автором коме комментария
        if (commentRepository.isAuthorOfComment(commentId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            commentRepository.updateLikeAmountAndUnsetMyLike(commentId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            commentRepository.updateLikeAmount(commentId);
        }

        log.info("Like removed from comment with id: {}", commentId);
    }

    private void checkPostPresence(UUID postId) {
        // Проверка существования поста
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException(
                    MessageFormat.format("Post with id {0} not found", postId));
        }
    }

    private void checkCommentPresence(UUID commentId) {
        // Проверка существования комментария
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException(
                    MessageFormat.format("Comment with id {0} not found", commentId));
        }
    }

    private void checkLikeDto(LikeDto likeDto) {
        if (likeDto == null) {
            throw new IllegalArgumentException("Like data must not be null");
        }
    }

    private UUID getAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }
}