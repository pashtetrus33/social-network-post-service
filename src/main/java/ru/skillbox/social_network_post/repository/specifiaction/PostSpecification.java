package ru.skillbox.social_network_post.repository.specifiaction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
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
    String AUTHOR_ID = "authorId";

    static Specification<Post> withFilters(PostSearchDto postSearchDto, UUID currentAccountId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            log.warn("Start building predicates for postSearchDto: {}", postSearchDto);

            addPostIdsPredicate(postSearchDto.getIds(), root, predicates);

            addAccountIdsPredicate(postSearchDto, currentAccountId, root, criteriaBuilder, predicates);

            addBlockedIdsPredicate(postSearchDto.getBlockedIds(), root, predicates);
            addBlockedStatusPredicate(postSearchDto.getIsBlocked(), root, criteriaBuilder, predicates);

            addDeletedStatusPredicate(postSearchDto.getIsDeleted(), root, criteriaBuilder, predicates);

            addTitlePredicate(postSearchDto.getTitle(), root, criteriaBuilder, predicates);

            addPostTextPredicate(postSearchDto.getPostText(), root, criteriaBuilder, predicates);

            addTagsPredicate(postSearchDto.getTags(), root, predicates);

            addDateFromPredicate(postSearchDto.getDateFrom(), root, criteriaBuilder, predicates);

            addDateToPredicate(postSearchDto.getDateTo(), root, criteriaBuilder, predicates);

            log.warn("Final predicates size: {}", predicates.size());

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addPostIdsPredicate(List<UUID> postIds, Root<Post> root, List<Predicate> predicates) {
        if (postIds != null && !postIds.isEmpty()) {
            log.warn("Adding predicate for post IDs: {}", postIds);
            predicates.add(root.get("id").in(postIds));
        } else {
            log.warn("No post IDs to filter.");
        }
    }

    private static void addAccountIdsPredicate(PostSearchDto postSearchDto, UUID currentAccountId, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {

        List<UUID> accountIds = postSearchDto.getAccountIds();
        log.warn("Post searchDto. Account IDs: {}", accountIds);

        if (accountIds != null) {
            log.warn("Adding predicate for account IDs: {}", accountIds);
            predicates.add(root.get(AUTHOR_ID).in(accountIds));
        } else {
            log.warn("No account IDs provided. Filtering only own posts for account: {}", currentAccountId);
            predicates.add(criteriaBuilder.notEqual(root.get(AUTHOR_ID), currentAccountId));
        }
    }

    private static void addBlockedIdsPredicate(List<UUID> blockedIds, Root<Post> root, List<Predicate> predicates) {
        if (blockedIds != null && !blockedIds.isEmpty()) {
            log.warn("Adding predicate for blocked post IDs: {}", blockedIds);
            predicates.add(root.get("id").in(blockedIds));
        } else {
            log.warn("No blocked post IDs to filter.");
        }
    }

    private static void addBlockedStatusPredicate(Boolean isBlocked, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (isBlocked != null && !isBlocked) {
            log.warn("Adding predicate for posts that are not blocked.");
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isFalse(root.get("isBlocked")),
                    criteriaBuilder.isNull(root.get("isBlocked"))
            ));
        } else {
            log.warn("No blocked status to filter.");
        }
    }

    private static void addDeletedStatusPredicate(Boolean isDeleted, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (isDeleted != null && !isDeleted) {
            log.warn("Adding predicate for posts that are not deleted.");
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isFalse(root.get("isDeleted")),
                    criteriaBuilder.isNull(root.get("isDeleted"))
            ));
        } else {
            log.warn("No deleted status to filter.");
        }
    }

    private static void addTitlePredicate(String title, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (title != null && !title.isBlank()) {
            log.warn("Adding predicate for post title: {}", title);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        } else {
            log.warn("No title to filter.");
        }
    }

    private static void addPostTextPredicate(String postText, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (postText != null && !postText.isBlank()) {
            log.warn("Adding predicate for post text: {}", postText);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("postText")), "%" + postText.toLowerCase() + "%"));
        } else {
            log.warn("No post text to filter.");
        }
    }

    private static void addTagsPredicate(List<String> tags, Root<Post> root, List<Predicate> predicates) {
        if (tags != null && !tags.isEmpty()) {
            log.warn("Adding predicate for post tags: {}", tags);
            Join<Post, String> tagsJoin = root.join("tags");
            predicates.add(tagsJoin.in(tags));
        } else {
            log.warn("No tags to filter.");
        }
    }

    private static void addDateFromPredicate(String dateFrom, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (dateFrom != null) {
            log.warn("Adding predicate for dateFrom: {}", dateFrom);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("publishDate"),
                    Instant.ofEpochMilli(Long.parseLong(dateFrom))
                            .atZone(ZoneOffset.UTC).toLocalDateTime()
            ));
        } else {
            log.warn("No dateFrom to filter.");
        }
    }

    private static void addDateToPredicate(String dateTo, Root<Post> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (dateTo != null) {
            log.warn("Adding predicate for dateTo: {}", dateTo);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("publishDate"),
                    Instant.ofEpochMilli(Long.parseLong(dateTo))
                            .atZone(ZoneOffset.UTC).toLocalDateTime()
            ));
        } else {
            log.warn("No dateTo to filter.");
        }
    }
}