package ru.skillbox.social_network_post.repository.specifiaction;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.skillbox.social_network_post.dto.CommentSearchDto;
import ru.skillbox.social_network_post.entity.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface CommentSpecification {

    static Specification<Comment> withFilters(CommentSearchDto commentSearchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addPredicate(predicates, commentSearchDto.getLikeAmount(),
                    amount -> criteriaBuilder.greaterThanOrEqualTo(root.get("likeAmount"), amount));

            addPredicate(predicates, commentSearchDto.getCommentsCount(),
                    count -> criteriaBuilder.greaterThanOrEqualTo(root.get("commentsCount"), count));

            addPredicate(predicates, commentSearchDto.getIsBlocked(),
                    isBlocked -> criteriaBuilder.equal(root.get("isBlocked"), isBlocked));

            addPredicate(predicates, commentSearchDto.getIsDeleted(),
                    isDeleted -> criteriaBuilder.equal(root.get("isDeleted"), isDeleted));

            addPredicate(predicates, commentSearchDto.getCommentType(),
                    type -> criteriaBuilder.equal(root.get("commentType"), type));

            addPredicate(predicates, commentSearchDto.getParentCommentId(),
                    parentId -> criteriaBuilder.equal(root.get("parentComment"), parentId));

            addPredicate(predicates, commentSearchDto.getPostId(),
                    postId -> criteriaBuilder.equal(root.get("post").get("id"), postId));

            Optional.ofNullable(commentSearchDto.getCommentText())
                    .filter(text -> !text.isBlank())
                    .ifPresent(text -> predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("commentText")), "%" + text.toLowerCase() + "%")));

            Optional.ofNullable(commentSearchDto.getImagePath())
                    .filter(path -> !path.isBlank())
                    .ifPresent(path -> predicates.add(criteriaBuilder.like(root.get("imagePath"), "%" + path + "%")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static <T> void addPredicate(List<Predicate> predicates, T value, Function<T, Predicate> predicateFunction) {
        Optional.ofNullable(value).ifPresent(v -> predicates.add(predicateFunction.apply(v)));
    }
}