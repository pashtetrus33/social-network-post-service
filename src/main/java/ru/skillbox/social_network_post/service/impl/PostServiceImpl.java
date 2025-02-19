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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.exception.PostNotFoundException;
import ru.skillbox.social_network_post.mapper.PostMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.PostSpecification;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.PostService;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
    @Cacheable(value = "posts", key = "#postId")
    @Transactional(readOnly = true)
    public PostDto getById(UUID postId) {
        log.info("Fetching post with id: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(
                        MessageFormat.format("Post with id {0} not found", postId)
                ));

        kafkaService.deletedAccountEvent(UUID.fromString("123e4567-e89b-12d3-a456-426614174777"));
        return PostMapperFactory.toPostDto(post);
    }

    @Override
    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public void update(UUID postId, PostDto postDto) {
        log.info("Updating post with id: {}", postId);

        if (!postId.equals(postDto.getId())) {
            throw new IdMismatchException(
                    MessageFormat.format("Id in body {0} and in path request {1} are different", postDto.getId(), postId));
        }

        Post post = checkPostPresence(postId);

        PostMapperFactory.updatePostFromDto(postDto, post);
        postRepository.save(post);
        log.info("Post with id: {} updated successfully", postId);
    }

    @Override
    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public void delete(UUID postId) {
        log.info("Deleting post with id: {}", postId);

        checkPostPresence(postId);


        commentRepository.deleteByPostId(postId);

        postRepository.deleteById(postId);
        log.info("Post with id: {} deleted successfully", postId);
    }

    @Override
    @Cacheable(value = "post_pages", key = "#searchDto.toString() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagePostDto getAll(@Valid PostSearchDto searchDto, Pageable pageable) {
        log.info("Fetching all posts with pageable: {}", pageable);

        if (searchDto.getAuthor() != null && !searchDto.getAuthor().isBlank()) {
            AccountSearchDto accountSearchDto = new AccountSearchDto();
            accountSearchDto.setAuthor(searchDto.getAuthor());

            // Вызов Feign-клиента с обработкой ошибок
            try {
                //authorId = accountServiceClient.getAccountByName(accountSearchDto);
                authorId = UUID.fromString("123e4567-e89b-12d3-a456-426614174777");

            } catch (FeignException e) {
                throw new CustomFreignException(MessageFormat.format("Error fetching account by name: {0}", searchDto.getAuthor()));
            }
        }

        //Test kafka
        kafkaService.blockedAccountEvent(UUID.fromString("123e4567-e89b-12d3-a456-426614174777"));

        if (searchDto.getWithFriends() != null && searchDto.getWithFriends().equals(true)) {

            // Вызов Feign-клиента с обработкой ошибок
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                userId = (UUID) authentication.getPrincipal();
                //friendsIds = friendServiceClient.getFriendsIds(userId);
                friendsIds.add(UUID.fromString("123e4567-e89b-12d3-a456-426614174777"));

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
        log.info("Creating new post");

        Post post = PostMapperFactory.toPost(postDto);
        if (publishDate != null) {
            LocalDateTime publishDateTime = Instant.ofEpochMilli(publishDate)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime();
            post.setPublishDate(publishDateTime);
        } else {
            post.setPublishDate(LocalDateTime.now(ZoneId.of("UTC")));
        }

        post.setId(null); // Сбрасываем ID, чтобы Hibernate сгенерировал новый

        postRepository.save(post);
        log.info("Created post with ID {} successfully", post.getId());

        KafkaDto kafkaDto = new KafkaDto(MessageFormat.format("Post with id {0} created successfully", post.getId()));

        kafkaService.newPostEvent(kafkaDto);
    }

    @Override
    @Cacheable(value = "imageCache", key = "#file.originalFilename")
    @Transactional
    public String uploadPhoto(MultipartFile file) {
        log.info("Uploading photo: {}", file.getOriginalFilename());
        // File upload logic (should be implemented properly)
        String uploadedPath = file.getName();
        log.info("Photo uploaded successfully: {}", uploadedPath);
        return uploadedPath;
    }

    @Override
    public List<Post> getAllByAccountId(UUID accountId) {
        return postRepository.findAllByAuthorId(accountId);
    }

    @Override
    public void saveAll(List<Post> posts) {
        postRepository.saveAll(posts);
    }

    private Post checkPostPresence(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new PostNotFoundException(MessageFormat.format("Post with id {0} not found", postId));
                });
    }
}