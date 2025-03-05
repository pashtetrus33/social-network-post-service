package ru.skillbox.social_network_post.service.impl;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import java.util.*;


@Service
public class PostServiceImpl implements PostService {

    private final AccountServiceClient accountServiceClient;
    private final FriendServiceClient friendServiceClient;
    private final KafkaService kafkaService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private List<UUID> authorIds = new ArrayList<>();
    private List<UUID> friendsIds = new ArrayList<>();
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

    @Cacheable(value = "posts", key = "#postId")
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


    @Cacheable(value = "post_pages", key = "{#searchDto.author, #searchDto.withFriends, #searchDto.dateTo, #pageable.pageNumber, #pageable.pageSize}")
    @LogExecutionTime
    @Override
    public PagePostDto getAll(@Valid PostSearchDto searchDto, Pageable pageable) {

        // Проверка автора и получение его ID
        if (searchDto.getAuthor() != null && !searchDto.getAuthor().isBlank()) {
            try {

                // Получаем список идентификаторов по имени автора из сервиса аккаунтов
                //authorIds = getAuthorIds(searchDto.getAuthor());

                //TODO: Убрать тестовую заглушку
                authorIds = new ArrayList<>(Collections.singleton(UUID.fromString("6f6d7a8f-1243-42cf-b4dd-287f3ef60eb0")));

                searchDto.setAccountIds(authorIds);

            } catch (FeignException e) {
                throw new CustomFreignException(MessageFormat.format("Error fetching accounts by name: {0}", searchDto.getAuthor()));
            }
        }

        // Проверка флага с друзьями и получение их ID
        if (Boolean.TRUE.equals(searchDto.getWithFriends())) {
            accountId = SecurityUtils.getAccountId();
            try {

                // Получаем список друзей из сервиса друзей
                //friendsIds = getFriendsIds(accountId);

                //TODO: Убрать тестовую заглушку
                friendsIds.add(UUID.fromString("6f6d7a8f-1243-42cf-b4dd-287f3ef60eba"));

                searchDto.setAccountIds(friendsIds);

            } catch (Exception e) {
                throw new CustomFreignException(MessageFormat.format("Error fetching friends for accountId: {0}", accountId));
            }
        }

        if (searchDto.getDateTo() == null) {
            searchDto.setDateTo(String.valueOf(Instant.now().toEpochMilli()));
        } else {
            searchDto.setDateTo(String.valueOf(Instant.parse(searchDto.getDateTo()).toEpochMilli()));
        }
        if (searchDto.getDateFrom() != null) {
            searchDto.setDateFrom(String.valueOf(Instant.parse(searchDto.getDateFrom()).toEpochMilli()));
        }
        // Формируем спецификацию для поиска
        Specification<Post> spec = PostSpecification.withFilters(searchDto);

        // Запрашиваем посты из репозитория
        Page<Post> posts = postRepository.findAll(spec, pageable);

        // Преобразуем результат в DTO и возвращаем
        return PostMapperFactory.toPagePostDto(posts);
    }


    // Очистка кэша при изменении данных
    @Caching(evict = {
            @CacheEvict(value = "posts", key = "#postId"),
            @CacheEvict(value = "post_pages", allEntries = true),
            @CacheEvict(value = "comments", key = "#postId")
    })
    @LogExecutionTime
    @Override
    @Transactional
    public void create(PostDto postDto) {

        EntityCheckUtils.checkPostDto(postDto);

        accountId = SecurityUtils.getAccountId();

        Post post = PostMapperFactory.toPost(postDto);

        // Устанавливаем publishDate, если он передан, иначе текущее время
        if (postDto.getPublishDate() != null) {
            post.setPublishDate(postDto.getPublishDate());
        } else {
            post.setPublishDate(LocalDateTime.now(ZoneOffset.UTC));

        }

        post.setAuthorId(accountId);
        post.setId(null);// Сбрасываем ID, чтобы Hibernate сгенерировал новый
        post.setIsBlocked(false);
        post.setIsDeleted(false);
        post.setReactionsCount(0);
        post.setCommentsCount(0);

        postRepository.save(post);
        kafkaService.newPostEvent(new KafkaDto(accountId, post.getId()));
    }


    // Очистка кэша при изменении данных
    @Caching(evict = {
            @CacheEvict(value = "posts", key = "#postId"),
            @CacheEvict(value = "post_pages", allEntries = true),
            @CacheEvict(value = "comments", key = "#postId")
    })
    @LogExecutionTime
    @Override
    @Transactional
    public void update(UUID postId, PostDto postDto) {

        EntityCheckUtils.checkPostPresence(postRepository, postId);

        EntityCheckUtils.checkPostDto(postDto);

        if (!Objects.equals(postId, postDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", postDto.getId(), postId));
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    return new EntityNotFoundException("Post with ID " + postId + " not found");
                });

        if (postDto.getTimeChanged() == null) {
            post.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));
        }

        PostMapperFactory.updatePostFromDto(postDto, post);
    }


    // Очистка кэша при изменении данных
    @Caching(evict = {
            @CacheEvict(value = "posts", key = "#postId"),
            @CacheEvict(value = "post_pages", allEntries = true),
            @CacheEvict(value = "comments", key = "#postId")
    })
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
    public List<UUID> getFriendsIds(UUID accountId) {
        try {
            //TODO:
            return null; //friendServiceClient.getFriendsIds(accountId);
        } catch (FeignException e) {
            throw new CustomFreignException(MessageFormat.format("Error fetching friends by accountId: {0}", accountId));
        }
    }


    @LogExecutionTime
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public List<UUID> getAuthorIds(@Size(max = 255, message = "Author name must not exceed 255 characters") String author) {
        try {
            AccountSearchDto accountSearchDto = new AccountSearchDto();
            accountSearchDto.setAuthor(author);
            //TODO:
            return null; //accountServiceClient.searchAccount(accountSearchDto).stream().map(AccountDto::getId).toList();
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
    public void updateDeletedStatusForAccount(UUID uuid) {
        postRepository.updateDeletedStatusForAccount(uuid);
    }
}