package ru.skillbox.social_network_post.service.impl;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.entity.PostType;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.mapper.PostMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.specifiaction.PostSpecification;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.PostService;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final AccountServiceClient accountServiceClient;
    private final FriendServiceClient friendServiceClient;
    private final KafkaService kafkaService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private UUID authorId;
    private UUID userId;
    private final List<UUID> friendsIds = new ArrayList<>();

    public PostServiceImpl(AccountServiceClient accountServiceClient,
                           FriendServiceClient friendServiceClient, @Lazy KafkaService kafkaService,
                           PostRepository postRepository, CommentRepository commentRepository) {
        this.accountServiceClient = accountServiceClient;
        this.friendServiceClient = friendServiceClient;
        this.kafkaService = kafkaService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Post> getAllByAccountId(UUID accountId) {
        return postRepository.findAllByAuthorId(accountId);
    }


    @Override
    @Cacheable(value = "posts", key = "#postId")
    @Transactional(readOnly = true)
    public PostDto getById(UUID postId) {

        log.info("Fetching post with id: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Post with id {0} not found", postId)
                ));

        return PostMapperFactory.toPostDto(post);
    }

    @Transactional
    @Override
    @Cacheable(value = "post_pages", key = "#searchDto.toString() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PagePostDto getAll(@Valid PostSearchDto searchDto, Pageable pageable) {
        log.info("Fetching all posts with pageable: {}", pageable);

        if (searchDto.getAuthor() != null && !searchDto.getAuthor().isBlank()) {
            //AccountSearchDto accountSearchDto = new AccountSearchDto();
            //accountSearchDto.setAuthor(searchDto.getAuthor());

            // Вызов Feign-клиента с обработкой ошибок
            try {
                //authorId = accountServiceClient.getAccountByName(accountSearchDto);
                authorId = UUID.fromString("6f6d7a8f-1243-42cf-b4dd-287f3ef60eb0");

            } catch (FeignException e) {
                throw new CustomFreignException(MessageFormat.format("Error fetching account by name: {0}", searchDto.getAuthor()));
            }
        }

        if (searchDto.getWithFriends() != null && searchDto.getWithFriends().equals(true)) {

            // Вызов Feign-клиента с обработкой ошибок
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                userId = (UUID) authentication.getPrincipal();
                //friendsIds = friendServiceClient.getFriendsIds(userId);
                friendsIds.add(UUID.fromString("6f6d7a8f-1243-42cf-b4dd-287f3ef60eba"));

                searchDto.setAccountIds(friendsIds);

            } catch (FeignException e) {
                throw new CustomFreignException(MessageFormat.format("Error fetching friends by accountId: {0}", userId));
            }
        }

        Specification<Post> spec = PostSpecification.withFilters(searchDto, authorId);

        Page<Post> posts = postRepository.findAll(spec, pageable);
        return PostMapperFactory.toPagePostDto(posts);
    }

    @Override
    @CacheEvict(value = {"posts", "post_pages"}, allEntries = true)
    @Transactional
    public void create(PostDto postDto, Long publishDate) {

        checkPostDto(postDto);

        getUserInfo(postDto);

        Post post = PostMapperFactory.toPost(postDto);

        // Устанавливаем publishDate, если он передан, иначе текущее время
        if (publishDate != null) {
            post.setPublishDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(publishDate), ZoneOffset.UTC));
            post.setType(PostType.QUEUED);
        } else {
            post.setPublishDate(LocalDateTime.now(ZoneOffset.UTC));
            post.setType(PostType.POSTED);
        }

        post.setId(null);// Сбрасываем ID, чтобы Hibernate сгенерировал новый
        post.setIsBlocked(false);
        post.setIsDeleted(false);
        post.setLikeAmount(0);
        post.setCommentsCount(0);

        postRepository.save(post);
        log.info("Created post with ID {} successfully", post.getId());

        kafkaService.newPostEvent(
                new KafkaDto(
                        MessageFormat.format("Post with id {0} created successfully", post.getId())));
    }

    private static void getUserInfo(PostDto postDto) {
        // Получаем Authentication из SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Извлекаем роли
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        postDto.setAuthorId((UUID) authentication.getPrincipal());

        log.info("Creating new post by User: {} userId: {} with roles: {}",
                authentication.getName(), postDto.getAuthorId(), roles);
    }

    @Override
    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public void update(UUID postId, PostDto postDto) {
        log.info("Updating post with id: {}", postId);

        checkPostPresence(postId);
        checkPostDto(postDto);

        if (!Objects.equals(postId, postDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", postDto.getId(), postId));
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new EntityNotFoundException("Post with ID " + postId + " not found");
                });

        if (postDto.getTimeChanged() == null) {
            post.setTimeChanged(LocalDateTime.now(ZoneOffset.UTC));
        }

        PostMapperFactory.updatePostFromDto(postDto, post);

        log.info("Post with id: {} updated successfully", postId);
    }


    @Override
    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public void delete(UUID postId) {
        log.info("Deleting post with id: {}", postId);

        checkPostPresence(postId);

        // Помечаем все комментарии поста как удаленные
        commentRepository.markAllAsDeletedByPostId(postId);

        // Обновляем флаг удаления поста без загрузки сущности
        postRepository.markAsDeleted(postId);

        log.info("Post with id: {} marked as deleted", postId);

        // Отправляем сообщение в Kafka о пометке поста как удалённого
        kafkaService.newPostEvent(
                new KafkaDto(MessageFormat.format("Post with id {0} marked as deleted", postId))
        );
    }


    @Override
    public void saveAll(List<Post> posts) {
        postRepository.saveAll(posts);
    }

    private void checkPostPresence(UUID postId) {
        // Проверка существования поста
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException(
                    MessageFormat.format("Post with id {0} not found", postId));
        }
    }


    private void checkPostDto(PostDto postDto) {
        if (postDto == null) {
            throw new IllegalArgumentException("Post data must not be null");
        }
    }
}