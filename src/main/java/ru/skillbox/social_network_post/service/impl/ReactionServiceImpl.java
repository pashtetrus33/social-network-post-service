package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.dto.ReactionNotificationDto;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.dto.ReactionType;
import ru.skillbox.social_network_post.dto.RequestReactionDto;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private UUID accountId;
    private final KafkaService kafkaService;


    @LogExecutionTime
    @Override
    @Transactional
    public ReactionDto addLikeToPost(UUID postId, RequestReactionDto requestReactionDto) {

        Post post = EntityCheckUtils.checkPostPresence(postRepository, postId);
        EntityCheckUtils.checkReactionDto(requestReactionDto);

        accountId = SecurityUtils.getAccountId();

        Reaction reaction = LikeMapperFactory.toReaction(requestReactionDto);
        reaction.setPost(post);
        reaction.setAuthorId(accountId);


        // Проверяем, ставил ли пользователь реакцию на пост
        if (reactionRepository.existsByPostIdAndAuthorIdAndCommentIdIsNull(postId, accountId)) {
            log.warn("Реакция уже есть...");

            // Получаем реакцию, если она есть
            Optional<Reaction> reactionOptional = reactionRepository.findByPostIdAndAuthorId(postId, accountId);
            reactionOptional.ifPresent(reactionRepository::delete);
        }

        reactionRepository.save(reaction);

        postRepository.incrementReactionsCount(postId);

        List<ReactionDto.ReactionInfo> reactionInfoList = getReactionInfos(postId);

        long totalReactions = reactionRepository.countByPostId(postId);


        ReactionNotificationDto reactionNotificationDto = ReactionNotificationDto.builder()
                .authorId(accountId)
                .reactionId(reaction.getId())
                .postId(postId)
                .commentId(reaction.getCommentId())
                .reactionType(reaction.getReactionType())
                .publishDate(reaction.getCreatedAt())
                .build();

        kafkaService.newLikeEvent(reactionNotificationDto);


        return ReactionDto.builder()
                .active(true)
                .reactionsInfo(reactionInfoList)
                .reaction(reaction.getReactionType())
                .quantity(totalReactions)
                .build();
    }

    @Override
    public List<ReactionDto.ReactionInfo> getReactionInfos(UUID postId) {
        // Запрос к БД, получаем только те реакции, которые есть
        List<Object[]> dbResults = reactionRepository.countReactionsByPostId(postId);

        // Преобразуем в Map для удобства
        Map<String, Long> reactionCountMap = dbResults.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));

        // Гарантируем наличие всех реакций
        List<ReactionDto.ReactionInfo> reactionInfos = new ArrayList<>();
        for (ReactionType type : ReactionType.values()) {
            reactionInfos.add(new ReactionDto.ReactionInfo(
                    type.getName(),
                    reactionCountMap.getOrDefault(type.name(), 0L)
            ));
        }

        return reactionInfos;
    }

    @Override
    public String getMyReaction(UUID postId, UUID accountId) {
        return reactionRepository.findByPostIdAndAuthorId(postId, accountId)
                .map(Reaction::getReactionType)
                .orElse(null);
    }

    @Override
    public Boolean getMyReactionToComment(UUID postId, UUID commentId, UUID accountId) {
        return reactionRepository.existsByPostIdAndCommentIdAndAuthorId(postId, commentId, accountId);
    }


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


        if (reactionRepository.existsByPostIdAndAuthorIdAndCommentIdIsNull(postId, accountId)) {
            reactionRepository.deleteByPostIdAndAuthorId(postId, accountId);

            postRepository.updateReactionsCount(postId);

        } else {
            log.warn("У текущего пользователя нет реакции на этот пост. Пользователь {}", accountId);
        }
    }


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
            log.error("Like already exists for post with id {} and comment with id {}", postId, commentId);
            return;
        }

        Reaction reaction = new Reaction();
        reaction.setPost(post);
        reaction.setCommentId(commentId);
        reaction.setAuthorId(accountId);
        reaction.setType("No_type");
        reaction.setReactionType("No_reaction");

        if (reaction.getCreatedAt() == null) {
            reaction.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        }

        reactionRepository.save(reaction);

        commentRepository.incrementLikeAmount(commentId);

        ReactionNotificationDto reactionNotificationDto = ReactionNotificationDto.builder()
                .authorId(accountId)
                .reactionId(reaction.getId())
                .postId(postId)
                .commentId(reaction.getCommentId())
                .reactionType(reaction.getReactionType())
                .publishDate(reaction.getCreatedAt())
                .build();

        kafkaService.newLikeEvent(reactionNotificationDto);
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

        commentRepository.updateLikeAmount(commentId);

        reactionRepository.deleteByCommentIdAndAuthorId(commentId, accountId);
    }
}