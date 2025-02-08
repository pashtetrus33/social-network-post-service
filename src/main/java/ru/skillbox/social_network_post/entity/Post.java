package ru.skillbox.social_network_post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    private LocalDateTime time;

    @NotNull
    @PastOrPresent
    private LocalDateTime timeChanged;

    @NotNull
    @Positive
    private Long authorId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PostType type;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String postText;

    @NotNull
    private Boolean isBlocked;

    @NotNull
    private Boolean isDelete;

    @Min(0)
    private Integer commentsCount;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Min(0)
    private Integer likeAmount;

    @NotNull
    private Boolean myLike;

    @Size(max = 512)
    private String imagePath;

    @NotNull
    @PastOrPresent
    private LocalDateTime publishDate;
}