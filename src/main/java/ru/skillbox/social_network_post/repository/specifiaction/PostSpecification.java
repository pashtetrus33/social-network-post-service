package ru.skillbox.social_network_post.repository.specifiaction;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.dto.PostSearchDto;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostSpecification {

    public static Specification<Post> withFilters(PostSearchDto searchDto, UUID authorId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтрация по ID постов
            if (searchDto.getIds() != null && !searchDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(searchDto.getIds()));
            }

            // Фильтрация по ID аккаунтов авторов
            if (searchDto.getAccountIds() != null && !searchDto.getAccountIds().isEmpty()) {
                predicates.add(root.get("authorId").in(searchDto.getAccountIds()));
            }

            // Фильтрация по заблокированным постам
            if (searchDto.getBlockedIds() != null && !searchDto.getBlockedIds().isEmpty()) {
                predicates.add(root.get("id").in(searchDto.getBlockedIds()));
            }

            // Фильтрация по признаку блокировки поста
            if (searchDto.getIsBlocked() != null && searchDto.getIsBlocked()) {
                predicates.add(criteriaBuilder.isTrue(root.get("isBlocked")).not());
            }

            //Фильтрация по названию поста
            if (searchDto.getTitle() != null && !searchDto.getTitle().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchDto.getTitle().toLowerCase() + "%"));
            }

            // Фильтрация по тексту поста
            if (searchDto.getPostText() != null && !searchDto.getPostText().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("postText")), "%" + searchDto.getPostText().toLowerCase() + "%"));
            }

            // Фильтрация по статусу удаления поста
            if (searchDto.getIsDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), searchDto.getIsDeleted()));
            }

            // Фильтрация по тегам
            if (searchDto.getTags() != null && !searchDto.getTags().isEmpty()) {
                Join<Post, String> tagsJoin = root.join("tags");
                predicates.add(tagsJoin.in(searchDto.getTags()));
            }

            // Фильтрация по дате публикации (с)
            if (searchDto.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("publishDate"),
                        Instant.ofEpochMilli(searchDto.getDateFrom()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }

            // Фильтрация по дате публикации (по)
            if (searchDto.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("publishDate"),
                        Instant.ofEpochMilli(searchDto.getDateTo()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }

            // Фильтрация по автору поста
            if (searchDto.getAuthor() != null && !searchDto.getAuthor().isBlank()) {
                if (authorId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("authorId"), authorId));
                } else {
                    // Если автор не указан, игнорируем это условие
                    return criteriaBuilder.disjunction();
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}