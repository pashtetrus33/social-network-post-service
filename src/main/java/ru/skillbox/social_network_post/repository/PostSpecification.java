package ru.skillbox.social_network_post.repository;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.web.model.PostSearchDto;

import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {

    public static Specification<Post> withFilters(PostSearchDto searchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDto.getIds() != null && !searchDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(searchDto.getIds()));
            }
//            if (searchDto.getAccountIds() != null && !searchDto.getAccountIds().isEmpty()) {
//                predicates.add(root.get("accountId").in(searchDto.getAccountIds()));
//            }
            if (searchDto.getBlockedIds() != null && !searchDto.getBlockedIds().isEmpty()) {
                predicates.add(criteriaBuilder.not(root.get("id").in(searchDto.getBlockedIds())));
            }
//            if (searchDto.getAuthor() != null && !searchDto.getAuthor().isBlank()) {
//                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + searchDto.getAuthor().toLowerCase() + "%"));
//            }
            if (searchDto.getTitle() != null && !searchDto.getTitle().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchDto.getTitle().toLowerCase() + "%"));
            }
            if (searchDto.getPostText() != null && !searchDto.getPostText().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("postText")), "%" + searchDto.getPostText().toLowerCase() + "%"));
            }
//            if (searchDto.getWithFriends() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("withFriends"), searchDto.getWithFriends()));
//            }
            if (searchDto.getIsDelete() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDelete"), searchDto.getIsDelete()));
            }
            if (searchDto.getTags() != null && !searchDto.getTags().isEmpty()) {
                Join<Post, String> tagsJoin = root.join("tags");
                predicates.add(tagsJoin.in(searchDto.getTags()));
            }
            if (searchDto.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("publishDate"),
                        Instant.ofEpochMilli(searchDto.getDateFrom()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }
            if (searchDto.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("publishDate"),
                        Instant.ofEpochMilli(searchDto.getDateTo()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
