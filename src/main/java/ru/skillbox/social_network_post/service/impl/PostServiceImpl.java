package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.PostNotFoundException;
import ru.skillbox.social_network_post.mapper.PostMapper;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.web.model.PagePostDto;
import ru.skillbox.social_network_post.web.model.PostDto;
import ru.skillbox.social_network_post.web.model.PostSearchDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;


    @Override
    public PostDto getById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post with id " + postId + " not found"));
        return postMapper.toPostDto(post);
    }

    @Override
    public void update(Long postId, PostDto postDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post with id " + postId + " not found"));
        postMapper.updatePostFromDto(postDto, post);
        postRepository.save(post);
    }

    @Override
    public void delete(Long postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public PagePostDto getAll(PostSearchDto searchDto, Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable); // Add your search criteria here
        return postMapper.toPagePostDto(posts);
    }

    @Override
    public void create(PostDto postDto, Long publishDate) {
        // Преобразуем Long в LocalDateTime (если это миллисекунды)
        LocalDateTime publishDateTime = Instant.ofEpochMilli(publishDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Post post = postMapper.toPost(postDto);
        post.setPublishDate(publishDateTime);
        postRepository.save(post);
    }

    @Override
    public String uploadPhoto(MultipartFile file) {
        // Логика загрузки файла
        String imagePath = "uploaded/image/path"; // Тут должен быть код для сохранения изображения
        return imagePath;
    }
}