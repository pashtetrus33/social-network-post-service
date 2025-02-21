package ru.skillbox.social_network_post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts", schema = "schema_post")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @CreationTimestamp
    private LocalDateTime time;

    @UpdateTimestamp
    private LocalDateTime timeChanged;

    private UUID authorId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String postText;

    private Boolean isBlocked;

    private Boolean isDeleted;

    @Min(0)
    private Integer commentsCount;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"), schema = "schema_post")
    @Column(name = "tag")
    private List<String> tags;

    @Min(0)
    private Integer likeAmount;

    @NotNull
    private Boolean myLike;

    @Size(max = 512)
    private String imagePath;

    private LocalDateTime publishDate;
}