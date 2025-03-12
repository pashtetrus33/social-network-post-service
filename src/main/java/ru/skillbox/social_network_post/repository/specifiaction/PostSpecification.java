package ru.skillbox.social_network_post.repository.specifiaction;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.dto.PostSearchDto;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public interface PostSpecification {

    static Specification<Post> withFilters(PostSearchDto postSearchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтрация по ID постов
            if (postSearchDto.getIds() != null && !postSearchDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(postSearchDto.getIds()));
            }

            // Фильтрация по ID аккаунтов авторов
            if (postSearchDto.getAccountIds() != null) {
                predicates.add(root.get("authorId").in(postSearchDto.getAccountIds()));
            }

            // Фильтрация по заблокированным постам
            if (postSearchDto.getBlockedIds() != null && !postSearchDto.getBlockedIds().isEmpty()) {
                predicates.add(root.get("id").in(postSearchDto.getBlockedIds()));
            }

            // Фильтрация по признаку блокировки поста
            if (postSearchDto.getIsBlocked() != null && !postSearchDto.getIsBlocked()) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isFalse(root.get("isBlocked")),
                        criteriaBuilder.isNull(root.get("isBlocked"))
                ));
            }


            // Фильтрация по статусу удаления поста
            if (postSearchDto.getIsDeleted() != null && !postSearchDto.getIsDeleted()) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isFalse(root.get("isDeleted")),
                        criteriaBuilder.isNull(root.get("isDeleted"))
                ));
            }

            // Фильтрация по названию поста
            if (postSearchDto.getTitle() != null && !postSearchDto.getTitle().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + postSearchDto.getTitle().toLowerCase() + "%"));
            }

            // Фильтрация по тексту поста
            if (postSearchDto.getPostText() != null && !postSearchDto.getPostText().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("postText")), "%" + postSearchDto.getPostText().toLowerCase() + "%"));
            }

            // Фильтрация по тегам
            if (postSearchDto.getTags() != null && !postSearchDto.getTags().isEmpty()) {
                Join<Post, String> tagsJoin = root.join("tags");
                predicates.add(tagsJoin.in(postSearchDto.getTags()));
            }

            // Фильтрация по дате публикации (с)
            if (postSearchDto.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("publishDate"),
                        Instant.ofEpochMilli(Long.parseLong(postSearchDto.getDateFrom()))
                                .atZone(ZoneOffset.UTC).toLocalDateTime()
                ));
            }
            // Фильтрация по дате публикации (по)
            if (postSearchDto.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("publishDate"),
                        Instant.ofEpochMilli(Long.parseLong(postSearchDto.getDateTo()))
                                .atZone(ZoneOffset.UTC).toLocalDateTime()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}