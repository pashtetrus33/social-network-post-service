package ru.skillbox.social_network_post.repository.specifiaction;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.skillbox.social_network_post.dto.CommentSearchDto;
import ru.skillbox.social_network_post.entity.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentSpecification {

    private CommentSpecification() {
    }

    public static Specification<Comment> withFilters(CommentSearchDto commentSearchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтрация по количеству лайков
            if (commentSearchDto.getLikeAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("likeAmount"),
                        commentSearchDto.getLikeAmount()));
            }

            // Фильтрация по количеству комментариев
            if (commentSearchDto.getCommentsCount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("commentsCount"),
                        commentSearchDto.getCommentsCount()));
            }

            // Фильтрация по статусу блокировки
            if (commentSearchDto.getIsBlocked() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isBlocked"), commentSearchDto.getIsBlocked()));
            }

            // Фильтрация по статусу удаления
            if (commentSearchDto.getIsDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), commentSearchDto.getIsDeleted()));
            }

            // Фильтрация по тексту комментария
            if (commentSearchDto.getCommentText() != null && !commentSearchDto.getCommentText().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("commentText")), "%%" +
                        commentSearchDto.getCommentText().toLowerCase() + "%%"));
            }

            // Фильтрация по пути изображения
            if (commentSearchDto.getImagePath() != null && !commentSearchDto.getImagePath().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("imagePath"), "%%" +
                        commentSearchDto.getImagePath() + "%%"));
            }

            // Фильтрация по типу комментария
            if (commentSearchDto.getCommentType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("commentType"), commentSearchDto.getCommentType()));
            }

            // Фильтрация по родительскому комментарию
            if (commentSearchDto.getParentCommentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentComment"), commentSearchDto.getParentCommentId()));
            }

            // Фильтрация по посту
            if (commentSearchDto.getPostId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("post").get("postId"), commentSearchDto.getPostId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}