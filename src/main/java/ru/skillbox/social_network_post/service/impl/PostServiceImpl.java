package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.exception.PostNotFoundException;
import ru.skillbox.social_network_post.mapper.PostMapperFactory;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.repository.PostSpecification;
import ru.skillbox.social_network_post.service.KafkaService;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.web.model.KafkaDto;
import ru.skillbox.social_network_post.web.model.PagePostDto;
import ru.skillbox.social_network_post.web.model.PostDto;
import ru.skillbox.social_network_post.web.model.PostSearchDto;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final KafkaService kafkaService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    @Cacheable(value = "posts", key = "#postId")
    @Transactional
    public PostDto getById(UUID postId) {
        log.info("Fetching post with id: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(
                        MessageFormat.format("Post with id {0} not found", postId)
                ));
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
    @Cacheable(value = "post_pages", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional
    public PagePostDto getAll(PostSearchDto searchDto, Pageable pageable) {
        log.info("Fetching all posts with pageable: {}", pageable);

        Specification<Post> spec = PostSpecification.withFilters(searchDto);

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
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            post.setPublishDate(publishDateTime);
        } else {
            post.setPublishDate(LocalDateTime.now());
        }

        post.setId(null); // Сбрасываем ID, чтобы Hibernate сгенерировал новый

        postRepository.save(post);
        log.info("Created post with ID {} successfully", post.getId());

        KafkaDto kafkaDto = new KafkaDto(MessageFormat.format("Post with id {0} created successfully", post.getId()));

        kafkaService.produce(kafkaDto);
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

    private Post checkPostPresence(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new PostNotFoundException(MessageFormat.format("Post with id {0} not found", postId));
                });
    }
}