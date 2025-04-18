package ru.skillbox.social_network_post.service.impl;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.mapper.PostMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.specifiaction.PostSpecification;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.service.ReactionService;
import ru.skillbox.social_network_post.utils.EntityCheckUtils;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;


@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final AccountServiceClient accountServiceClient;
    private final FriendServiceClient friendServiceClient;
    private final ReactionService reactionService;
    private final KafkaService kafkaService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private UUID accountId;

    public PostServiceImpl(AccountServiceClient accountServiceClient,
                           FriendServiceClient friendServiceClient, @Lazy ReactionService reactionService, @Lazy KafkaService kafkaService,
                           PostRepository postRepository, CommentRepository commentRepository) {
        this.accountServiceClient = accountServiceClient;
        this.friendServiceClient = friendServiceClient;
        this.reactionService = reactionService;
        this.kafkaService = kafkaService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }


    @LogExecutionTime
    @Override
    @Transactional(readOnly = true)
    public PostDto getById(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Post with id {0} not found", postId)
                ));

        PostDto postDto = PostMapperFactory.toPostDto(post);

        String myReaction = reactionService.getMyReaction(postDto.getId(), accountId);

        if (myReaction != null) {
            postDto.setMyReaction(myReaction);
            postDto.setMyLike(true);
        }

        postDto.setReactionType(reactionService.getReactionInfos(postDto.getId()));

        return postDto;
    }

    @LogExecutionTime
    @Override
    public PagePostDto getAll(@Valid PostSearchDto postSearchDto, Pageable pageable) {

        accountId = SecurityUtils.getAccountId();

        processAccountIds(postSearchDto);
        processDateFilters(postSearchDto, accountId);


        Specification<Post> spec = PostSpecification.withFilters(postSearchDto, accountId);

        Page<Post> posts = postRepository.findAll(spec, pageable);

        PagePostDto pagePostDto = PostMapperFactory.toPagePostDto(posts);

        pagePostDto.getContent().forEach(postDto -> {

            String myReaction = reactionService.getMyReaction(postDto.getId(), accountId);

            if (myReaction != null) {
                postDto.setMyReaction(myReaction);
                postDto.setMyLike(true);
            }
            postDto.setReactionType(reactionService.getReactionInfos(postDto.getId()));
        });

        return pagePostDto;
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void create(PostDto postDto) {

        EntityCheckUtils.checkPostDto(postDto);

        // Устанавливаем publishDate, если он передан, иначе текущее время
        if (postDto.getPublishDate() == null) {

            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

            postDto.setPublishDate(now);
            postDto.setTime(now);

        } else {
            postDto.setTime(postDto.getPublishDate());
        }

        Post post = PostMapperFactory.toPost(postDto);

        accountId = SecurityUtils.getAccountId();

        post.setAuthorId(accountId);
        post.setId(null);// Сбрасываем ID, чтобы Hibernate сгенерировал новый
        post.setIsBlocked(false);
        post.setIsDeleted(false);
        post.setReactionsCount(0L);
        post.setCommentsCount(0L);

        postRepository.save(post);

        PostNotificationDto postNotificationDto = PostNotificationDto.builder()
                .authorId(post.getAuthorId())
                .postId(post.getId())
                .title(post.getTitle())
                .publishDate(post.getPublishDate())
                .build();

        kafkaService.newPostEvent(postNotificationDto);
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void update(PostDto postDto) {

        UUID postId = postDto.getId();

        EntityCheckUtils.checkPostPresence(postRepository, postId);

        EntityCheckUtils.checkPostDto(postDto);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post with ID " + postId + " not found"));

        if (postDto.getTimeChanged() == null) {
            postDto.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));
        }

        PostMapperFactory.updatePostFromDto(postDto, post);
    }


    @LogExecutionTime
    @Override
    @Transactional
    public void delete(UUID postId) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);

        // Помечаем все комментарии поста как удаленные
        commentRepository.markAllAsDeletedByPostId(postId);

        // Обновляем флаг удаления поста без загрузки сущности
        postRepository.markAsDeleted(postId);
    }


    @LogExecutionTime
    @Retryable(backoff = @Backoff(delay = 2000))
    public List<UUID> getFriendsIds() {

        accountId = SecurityUtils.getAccountId();

        try {
            return friendServiceClient.getFriendsIds();
        } catch (FeignException e) {
            log.error("Error fetching friends ids by accountId: {}", accountId);
            throw new CustomFreignException(MessageFormat.format("Error fetching friends by accountId: {0}", accountId));
        }
    }


    @LogExecutionTime
    @Retryable(backoff = @Backoff(delay = 2000))
    public List<UUID> getAuthorIds(@Size(max = 255, message = "Author name must not exceed 255 characters") String author) {
        try {
            return accountServiceClient.searchAccount(author).getContent().stream().map(AccountDto::getId).toList();
        } catch (FeignException e) {
            throw new CustomFreignException(MessageFormat.format("Error fetching authorId by name: {0}", author));
        }
    }


    @Override
    @Transactional
    public void updateBlockedStatusForAccount(UUID uuid) {
        postRepository.updateBlockedStatusForAccount(uuid);
    }


    @Override
    @Transactional
    public void updateDeletedStatusForAccount(UUID uuid) {
        postRepository.updateDeletedStatusForAccount(uuid);
    }


    private void processAccountIds(PostSearchDto postSearchDto) {

        log.warn("PostSearchDto. AccountIds: {}", postSearchDto.getAccountIds());
        log.warn("PostSearchDto. Author: {}", postSearchDto.getAuthor());

        List<UUID> authorIds = Optional.ofNullable(postSearchDto.getAuthor())
                .filter(author -> !author.isBlank())
                .map(this::getAuthorIds)
                .orElse(Collections.emptyList());

        List<UUID> friendsIds = Boolean.TRUE.equals(postSearchDto.getWithFriends())
                ? getFriendsIds()
                : Collections.emptyList();

        log.warn("AuthorsIds from accounts service: {}", authorIds);
        log.warn("Friends ids from friends service: {}", friendsIds);

        if (postSearchDto.getAccountIds() == null && (!authorIds.isEmpty() || !friendsIds.isEmpty())) {
            postSearchDto.setAccountIds(new ArrayList<>());
        }

        Optional.ofNullable(postSearchDto.getAccountIds()).ifPresent(accountIds -> {
            accountIds.addAll(authorIds);
            accountIds.addAll(friendsIds);
        });

        log.warn("Final accountIds: {}", postSearchDto.getAccountIds() != null ? postSearchDto.getAccountIds().size() : "null");
        Optional.ofNullable(postSearchDto.getAccountIds()).ifPresent(ids -> ids.forEach(id -> log.info("Account: {}", id)));
    }

    private void processDateFilters(PostSearchDto postSearchDto, UUID accountId) {
        long nowMillis = Instant.now().toEpochMilli();
        log.warn("Current timestamp (UTC): {}", nowMillis);

        if (postSearchDto.getAccountIds() == null || postSearchDto.getAccountIds().size() != 1 || !accountId.equals(postSearchDto.getAccountIds().get(0))) {
            long dateTo = parseOrDefault(postSearchDto.getDateTo(), nowMillis);
            postSearchDto.setDateTo(String.valueOf(dateTo));
        }

        Long dateFrom = parseOrNull(postSearchDto.getDateFrom());


        postSearchDto.setDateFrom(dateFrom != null ? String.valueOf(dateFrom) : null);

        log.warn("Processed date filters - DateFrom: {}, DateTo: {}", postSearchDto.getDateFrom(), postSearchDto.getDateTo());
    }

    private long parseOrDefault(String date, long defaultValue) {
        return Optional.ofNullable(date)
                .map(this::parseOrThrow)
                .orElse(defaultValue);
    }

    private Long parseOrNull(String date) {
        return Optional.ofNullable(date)
                .map(this::parseOrThrow)
                .orElse(null);
    }

    private long parseOrThrow(String date) {
        try {
            long timestamp = Instant.parse(date).toEpochMilli();
            log.warn("Parsed date '{}' -> timestamp {}", date, timestamp);
            return timestamp;
        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}", date, e);
            throw new IllegalArgumentException("Invalid date format: " + date, e);
        }
    }
}