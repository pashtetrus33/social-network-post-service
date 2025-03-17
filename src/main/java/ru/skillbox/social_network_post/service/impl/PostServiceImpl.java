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
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.mapper.PostMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.specifiaction.PostSpecification;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.PostService;
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
    private final KafkaService kafkaService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private UUID accountId;

    public PostServiceImpl(AccountServiceClient accountServiceClient,
                           FriendServiceClient friendServiceClient, @Lazy KafkaService kafkaService,
                           PostRepository postRepository, CommentRepository commentRepository) {
        this.accountServiceClient = accountServiceClient;
        this.friendServiceClient = friendServiceClient;
        this.kafkaService = kafkaService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    //@Cacheable(value = "posts", key = "#postId")
    @LogExecutionTime
    @Override
    @Transactional(readOnly = true)
    public PostDto getById(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Post with id {0} not found", postId)
                ));

        return PostMapperFactory.toPostDto(post);
    }


    //@Cacheable(value = "post_pages", key = "{#searchDto.author, #searchDto.withFriends, #searchDto.dateTo, #pageable.pageNumber, #pageable.pageSize}")
    @LogExecutionTime
    @Override
    public PagePostDto getAll(@Valid PostSearchDto postSearchDto, Pageable pageable) {

        List<UUID> authorIds = new ArrayList<>();
        List<UUID> friendsIds = new ArrayList<>();

        // Проверка автора и получение его ID
        if (postSearchDto.getAuthor() != null && !postSearchDto.getAuthor().isBlank()) {
            // Получаем список идентификаторов по имени автора из сервиса аккаунтов
            authorIds.addAll(getAuthorIds(postSearchDto.getAuthor()));
            log.warn("AuthorsIds from accounts service: {}", authorIds);
        }

        // Проверка флага с друзьями и получение их ID
        if (postSearchDto.getWithFriends() != null && Boolean.TRUE.equals(postSearchDto.getWithFriends())) {
            friendsIds.addAll(getFriendsIds());
            log.warn("Friends ids from friends service: {}", friendsIds);
        }

        // Если флаги withFriends или author не заданы, оставляем accountIds как null
        if (postSearchDto.getAccountIds() == null &&
                (postSearchDto.getWithFriends() != null && Boolean.TRUE.equals(postSearchDto.getWithFriends())
                        || postSearchDto.getAuthor() != null)) {
            // Инициализация только если есть хотя бы один флаг
            postSearchDto.setAccountIds(new ArrayList<>());
        }

        // Если флаги withFriends или author присутствуют, добавляем соответствующие ID
        if ((postSearchDto.getWithFriends() != null && Boolean.TRUE.equals(postSearchDto.getWithFriends())) ||
                postSearchDto.getAuthor() != null) {
            if (postSearchDto.getAccountIds() == null) {
                postSearchDto.setAccountIds(new ArrayList<>());
            }

            // Добавляем авторов и друзей в список
            postSearchDto.getAccountIds().addAll(authorIds);
            postSearchDto.getAccountIds().addAll(friendsIds);
        }

        // Логирование для проверки
        log.warn("Final accountIds: {}", postSearchDto.getAccountIds() != null ? postSearchDto.getAccountIds().size() : "null");
        if (postSearchDto.getAccountIds() != null) {
            postSearchDto.getAccountIds().forEach(e -> log.info("Account: {}", e));
        }

        Instant now = Instant.now();

        if (postSearchDto.getDateTo() == null) {
            postSearchDto.setDateTo(String.valueOf(now.toEpochMilli()));
        } else {
            try {
                postSearchDto.setDateTo(String.valueOf(Instant.parse(postSearchDto.getDateTo()).toEpochMilli()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format for dateTo: " + postSearchDto.getDateTo(), e);
            }
        }

        if (postSearchDto.getDateFrom() != null) {
            try {
                postSearchDto.setDateFrom(String.valueOf(Instant.parse(postSearchDto.getDateFrom()).toEpochMilli()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format for dateFrom: " + postSearchDto.getDateFrom(), e);
            }
        }

        accountId = SecurityUtils.getAccountId();

        if (postSearchDto.getAccountIds() != null && postSearchDto.getAccountIds().contains(accountId)) {
            postSearchDto.setDateTo(null);
        }

        // Формируем спецификацию для поиска
        Specification<Post> spec = PostSpecification.withFilters(postSearchDto);

        // Запрашиваем посты из репозитория
        Page<Post> posts = postRepository.findAll(spec, pageable);

        // Преобразуем результат в DTO и возвращаем
        return PostMapperFactory.toPagePostDto(posts);
    }


    // Очистка кэша при изменении данных
//    @Caching(evict = {
//            @CacheEvict(value = "posts", key = "#postId"),
//            @CacheEvict(value = "post_pages", allEntries = true),
//            @CacheEvict(value = "comments", key = "#postId")
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void create(PostDto postDto) {

        EntityCheckUtils.checkPostDto(postDto);

        Post post = PostMapperFactory.toPost(postDto);

        // Устанавливаем publishDate, если он передан, иначе текущее время
        if (postDto.getPublishDate() != null) {
            post.setPublishDate(postDto.getPublishDate());
        } else {
            post.setPublishDate(LocalDateTime.now(ZoneOffset.UTC));

        }

        accountId = SecurityUtils.getAccountId();

        post.setAuthorId(accountId);
        post.setId(null);// Сбрасываем ID, чтобы Hibernate сгенерировал новый
        post.setIsBlocked(false);
        post.setIsDeleted(false);
        post.setReactionsCount(0);
        post.setCommentsCount(0);

        postRepository.save(post);

        KafkaDto kafkaDto = KafkaDto.builder()
                .accountId(accountId)
                .dataId(post.getId())
                .build();

        kafkaService.newPostEvent(kafkaDto);
    }


    // Очистка кэша при изменении данных
//    @Caching(evict = {
//            @CacheEvict(value = "posts", key = "#postId"),
//            @CacheEvict(value = "post_pages", allEntries = true),
//            @CacheEvict(value = "comments", key = "#postId")
//    })
    @LogExecutionTime
    @Override
    @Transactional
    public void update(PostDto postDto) {

        UUID postId = postDto.getId();

        EntityCheckUtils.checkPostPresence(postRepository, postId);

        EntityCheckUtils.checkPostDto(postDto);

        if (!Objects.equals(postId, postDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", postDto.getId(), postId));
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post with ID " + postId + " not found"));

        if (postDto.getTimeChanged() == null) {
            postDto.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));
        }

        PostMapperFactory.updatePostFromDto(postDto, post);
    }


    // Очистка кэша при изменении данных
//    @Caching(evict = {
//            @CacheEvict(value = "posts", key = "#postId"),
//            @CacheEvict(value = "post_pages", allEntries = true),
//            @CacheEvict(value = "comments", key = "#postId")
//    })
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
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
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
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public List<UUID> getAuthorIds(@Size(max = 255, message = "Author name must not exceed 255 characters") String author) {
        try {
            AccountSearchDto accountSearchDto = new AccountSearchDto();
            accountSearchDto.setAuthor(author);
            return accountServiceClient.searchAccount(accountSearchDto).stream().map(AccountDto::getId).toList();
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
}