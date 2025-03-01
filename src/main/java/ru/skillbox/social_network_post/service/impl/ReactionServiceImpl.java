package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.dto.KafkaDto;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.entity.Reaction;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.mapper.LikeMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.ReactionRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.ReactionService;
import ru.skillbox.social_network_post.utils.EntityCheckUtils;

import java.text.MessageFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private UUID accountId;
    private final KafkaService kafkaService;


//    @Caching(evict = {
//            @CacheEvict(value = "posts", allEntries = true), // Очистка всех постов из кэша
//            @CacheEvict(value = "post_pages", allEntries = true), // Очистка всех страниц постов
//            @CacheEvict(value = "comments", allEntries = true) // Очистка всех комментариев
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public ReactionDto addLikeToPost(UUID postId, ReactionDto reactionDto) {

        Post post = EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkReactionDto(reactionDto);

        accountId = SecurityUtils.getAccountId();

        // Проверяем, ставил ли пользователь лайк ранее
        if (reactionRepository.existsByPostIdAndAuthorIdAndCommentIdIsNull(postId, accountId)) {
            throw new IllegalStateException(
                    MessageFormat.format("Like already exists for post with id {0}", postId));
        }

        Reaction reaction = LikeMapperFactory.toLike(reactionDto);
        reaction.setPost(post);
        reaction.setAuthorId(accountId);
        reactionRepository.save(reaction);

        // Проверяем, является ли текущий пользователь автором поста
        if (postRepository.isAuthorOfPost(postId, accountId)) {
            // Увеличиваем количество лайков и устанавливаем флаг myLike в true только если пост принадлежит автору
            postRepository.incrementReactionsCountAndSetMyReaction(postId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            postRepository.incrementReactionsCount(postId);
        }

        Long count = reactionRepository.countByCommentIdAndReactionType(postId, reactionDto.getReactionType());

        kafkaService.newLikeEvent(new KafkaDto(accountId, reaction.getId()));

        return ReactionDto.builder()
                .type(reactionDto.getReactionType())
                .reactionType(reactionDto.getReactionType())
                .count(count)
                .build();
    }


//    @Caching(evict = {
//            @CacheEvict(value = "posts", allEntries = true), // Очистка всех постов из кэша
//            @CacheEvict(value = "post_pages", allEntries = true), // Очистка всех страниц постов
//            @CacheEvict(value = "comments", allEntries = true) // Очистка всех комментариев
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void removeLikeFromPost(UUID postId) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);

        int likeAmount = postRepository.getReactionsCount(postId);

        if (likeAmount <= 0) {
            throw new IllegalStateException(
                    MessageFormat.format("Невозможно удалить лайк с поста {0}: количество лайков уже 0", postId));
        }

        accountId = SecurityUtils.getAccountId();

        // Проверяем, является ли текущий пользователь автором поста
        if (postRepository.isAuthorOfPost(postId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            postRepository.updateReactionsCountAndUnsetMyReaction(postId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            postRepository.updateReactionsCount(postId);
        }

        reactionRepository.deleteByPostIdAndAuthorId(postId, accountId);
    }


//    @Caching(evict = {
//            @CacheEvict(value = "posts", allEntries = true), // Очистка всех постов из кэша
//            @CacheEvict(value = "post_pages", allEntries = true), // Очистка всех страниц постов
//            @CacheEvict(value = "comments", allEntries = true) // Очистка всех комментариев
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void addLikeToComment(UUID postId, UUID commentId) {

        Post post = EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkCommentPresence(commentRepository, commentId);

        accountId = SecurityUtils.getAccountId();

        // Проверка, существует ли комментарий с таким ID и связан ли он с постом
        if (!commentRepository.existsByPostIdAndId(postId, commentId)) {
            throw new EntityNotFoundException(
                    MessageFormat.format("Comment with id {0} not found for post with id {1}", commentId, postId));
        }


        // Проверяем, ставил ли пользователь лайк для данного комментария
        if (reactionRepository.existsByCommentIdAndAuthorId(commentId, accountId)) {
            throw new IllegalStateException(
                    MessageFormat.format("Like already exists for post with id {0} and comment with id {1}", postId, commentId));
        }

        Reaction reaction = new Reaction();
        reaction.setPost(post);
        reaction.setCommentId(commentId);
        reaction.setAuthorId(accountId);
        reaction.setType("No_type");
        reaction.setReactionType("No_reaction");
        reactionRepository.save(reaction);


        // Проверяем, является ли текущий пользователь автором коме комментария
        if (commentRepository.isAuthorOfComment(commentId, accountId)) {
            // Увеличиваем количество лайков на комментарий и устанавливаем флаг myLike в true только если пост принадлежит автору
            commentRepository.incrementLikeAmountAndSetMyLike(commentId);
        } else {
            // Если пост не принадлежит пользователю, просто увеличиваем количество лайков
            commentRepository.incrementLikeAmount(commentId);
        }

        kafkaService.newLikeEvent(new KafkaDto(accountId, reaction.getId()));
    }


//    @Caching(evict = {
//            @CacheEvict(value = "posts", allEntries = true), // Очистка всех постов из кэша
//            @CacheEvict(value = "post_pages", allEntries = true), // Очистка всех страниц постов
//            @CacheEvict(value = "comments", allEntries = true) // Очистка всех комментариев
//    })
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

        reactionRepository.deleteByCommentIdAndAuthorId(commentId, accountId);
    }
}