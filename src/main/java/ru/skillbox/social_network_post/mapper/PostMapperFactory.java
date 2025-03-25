package ru.skillbox.social_network_post.mapper;

import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.dto.TagDto;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostType;

import java.util.*;

public final class PostMapperFactory {

    private PostMapperFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static PostDto toPostDto(Post post) {
        if (post == null) {
            return null;
        }

        return PostDto.builder()
                .id(post.getId())
                .time(post.getTime())
                .timeChanged(post.getTimeChanged())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .type(PostType.POSTED)
                .postText(post.getPostText())
                .isBlocked(post.getIsBlocked())
                .isDeleted(post.getIsDeleted())
                .commentsCount(post.getCommentsCount())
                .tags(post.getTags() != null
                        ? post.getTags().stream()
                        .map(TagDto::new) // Преобразуем String в TagDto
                        .toList()
                        : Collections.emptyList()) // Если null → пустой список
                .likeAmount(post.getReactionsCount())
                .myLike(post.getMyReaction())
                .imagePath(post.getImagePath())
                .publishDate(post.getPublishDate())
                .build();
    }

    public static PagePostDto toPagePostDto(Page<Post> posts) {
        if (posts == null) {
            return null;
        }

        return PagePostDto.builder()
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .number(posts.getNumber())
                .size(posts.getSize())
                .content(posts.hasContent() ? toPostDtoList(posts.getContent()) : List.of())
                .sort(posts.getSort())
                .first(posts.isFirst())
                .last(posts.isLast())
                .numberOfElements(posts.getNumberOfElements())
                .pageable(posts.getPageable())
                .empty(posts.isEmpty())
                .build();
    }

    public static Post toPost(PostDto postDto) {
        if (postDto == null) {
            return null;
        }

        List<String> tags = Optional.ofNullable(postDto.getTags()) // если null → пустой список
                .orElse(Collections.emptyList())
                .stream()
                .map(TagDto::name)
                .filter(Objects::nonNull) // защищаемся от null-значений
                .toList();

        return Post.builder()
                .time(postDto.getTime())
                .timeChanged(postDto.getTimeChanged())
                .authorId(postDto.getAuthorId())
                .title(postDto.getTitle())
                .postText(postDto.getPostText())
                .isBlocked(postDto.getIsBlocked())
                .isDeleted(postDto.getIsDeleted())
                .commentsCount(postDto.getCommentsCount())
                .tags(tags)
                .reactionsCount(postDto.getLikeAmount())
                .myReaction(postDto.getMyLike())
                .imagePath(postDto.getImagePath())
                .publishDate(postDto.getPublishDate())
                .build();
    }

    public static void updatePostFromDto(PostDto postDto, Post post) {
        if (postDto == null || post == null) {
            return;
        }

        post.setTimeChanged(postDto.getTimeChanged());
        post.setTitle(postDto.getTitle());
        post.setPostText(postDto.getPostText());

        if (postDto.getTags() != null) {

            List<String> tags = Optional.of(postDto.getTags()) // если null → пустой список
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(TagDto::name)
                    .filter(Objects::nonNull) // защищаемся от null-значений
                    .toList();

            post.setTags(tags);
        } else {
            post.setTags(null);
        }
    }

    public static List<PostDto> toPostDtoList(List<Post> posts) {
        return (posts == null) ? List.of() : posts.stream().map(PostMapperFactory::toPostDto).toList();
    }
}