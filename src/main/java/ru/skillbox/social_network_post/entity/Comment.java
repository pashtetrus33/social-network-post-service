package ru.skillbox.social_network_post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments", schema = "schema_post")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CommentType commentType;

    @NotNull
    @PastOrPresent
    private LocalDateTime time;

    @NotNull
    @PastOrPresent
    private LocalDateTime timeChanged;

    @NotNull
    @Positive
    private Long authorId;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String commentText;

    @NotNull
    @JoinColumn(name = "post_id", nullable = false)
    @ManyToOne
    private Post post;

    @NotNull
    private Boolean isBlocked;

    @NotNull
    private Boolean isDelete;

    @Min(0)
    private Integer likeAmount;

    @NotNull
    private Boolean myLike;

    @Min(0)
    private Integer commentsCount;

    @Size(max = 512)
    private String imagePath;
}
