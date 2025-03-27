package ru.skillbox.social_network_post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID authorId;

    private LocalDateTime time;

    private LocalDateTime timeChanged;

    @Min(0)
    private Integer likeAmount;

    @Min(0)
    private Integer commentsCount;

    @NotNull
    private Boolean isBlocked;

    @NotNull
    private Boolean isDeleted;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String commentText;

    @Size(max = 512)
    private String imagePath;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CommentType commentType;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
