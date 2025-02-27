package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.dto.LikeDto;
import ru.skillbox.social_network_post.entity.Like;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.mapper.LikeMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.LikeRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.utils.EntityCheckUtils;

import java.text.MessageFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private UUID accountId;
    private final KafkaService kafkaService;


    @LogExecutionTime
    @Override
    @Transactional
    public String addLikeToPost(UUID postId, LikeDto likeDto) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkLikeDto(likeDto);

        accountId = SecurityUtils.getAccountId();

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

        kafkaService.newLikeEvent(new KafkaDto(accountId, like.getId()));

        return likeDto.reactionType();
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void removeLikeFromPost(UUID postId) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);

        int likeAmount = postRepository.getLikeAmount(postId);

        if (likeAmount <= 0) {
            throw new IllegalStateException(
                    MessageFormat.format("Невозможно удалить лайк с поста {0}: количество лайков уже 0", postId));
        }

        accountId = SecurityUtils.getAccountId();

        // Проверяем, является ли текущий пользователь автором поста
        if (postRepository.isAuthorOfPost(postId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            postRepository.updateLikeAmountAndUnsetMyLike(postId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            postRepository.updateLikeAmount(postId);
        }

        likeRepository.deleteByPostIdAndAuthorId(postId, accountId);
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void addLikeToComment(UUID postId, UUID commentId) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkCommentPresence(commentRepository, commentId);

        accountId = SecurityUtils.getAccountId();

        // Проверка, существует ли комментарий с таким ID и связан ли он с постом
        if (!commentRepository.existsByPostIdAndId(postId, commentId)) {
            throw new EntityNotFoundException(
                    MessageFormat.format("Comment with id {0} not found for post with id {1}", commentId, postId));
        }


        // Проверяем, ставил ли пользователь лайк для данного комментария
        if (likeRepository.existsByCommentIdAndAuthorId(commentId, accountId)) {
            throw new IllegalStateException(
                    MessageFormat.format("Like already exists for post with id {0} and comment with id {1}", postId, commentId));
        }

        Like like = new Like();
        like.setPostId(postId);
        like.setCommentId(commentId);
        like.setAuthorId(accountId);
        like.setType("No_type");
        like.setReactionType("No_reaction");
        likeRepository.save(like);


        // Проверяем, является ли текущий пользователь автором коме комментария
        if (commentRepository.isAuthorOfComment(commentId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            commentRepository.incrementLikeAmountAndSetMyLike(commentId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            commentRepository.incrementLikeAmount(commentId);
        }

        kafkaService.newLikeEvent(new KafkaDto(accountId, like.getId()));
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void removeLikeFromComment(UUID postId, UUID commentId) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkCommentPresence(commentRepository, commentId);

        int likeAmount = commentRepository.getLikeAmount(commentId);

        if (likeAmount <= 0) {
            throw new IllegalStateException(
                    MessageFormat.format("Cannot remove like from post {0} and comment {1}: like count is already 0", postId, commentId));
        }

        accountId = SecurityUtils.getAccountId();

        // Проверяем, является ли текущий пользователь автором коме комментария
        if (commentRepository.isAuthorOfComment(commentId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            commentRepository.updateLikeAmountAndUnsetMyLike(commentId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            commentRepository.updateLikeAmount(commentId);
        }

        likeRepository.deleteByCommentIdAndAuthorId(commentId, accountId);
    }
}