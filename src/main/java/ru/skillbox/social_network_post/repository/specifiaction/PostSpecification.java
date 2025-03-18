package ru.skillbox.social_network_post.repository.specifiaction;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.dto.PostSearchDto;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public interface PostSpecification {

    Logger log = LoggerFactory.getLogger(PostSpecification.class);

    static Specification<Post> withFilters(PostSearchDto postSearchDto, UUID currentAccountId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтрация по ID постов
            if (postSearchDto.getIds() != null && !postSearchDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(postSearchDto.getIds()));
            }


            if (postSearchDto.getAccountIds() != null) {
                log.warn("Post specification.Account IDs provided for filtering: {}", postSearchDto.getAccountIds());
                predicates.add(root.get("authorId").in(postSearchDto.getAccountIds()));

            } else {
                // Если accountIds null, исключаем только свои посты
                log.warn("Post specification.No account IDs provided for filtering. Filter only own posts: {}"
                        , currentAccountId);
                predicates.add(criteriaBuilder.notEqual(root.get("authorId"), currentAccountId)); // Фильтрация по текущему аккаунту
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