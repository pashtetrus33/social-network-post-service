package ru.skillbox.social_network_post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID authorId;

    @CreationTimestamp
    private LocalDateTime time;

    @UpdateTimestamp
    private LocalDateTime timeChanged;

    @NotNull
    private LocalDateTime publishDate;

    @Min(0)
    private Integer likeAmount = 0;

    @Min(0)
    private Integer commentsCount = 0;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String postText;

    private Boolean isBlocked;

    private Boolean isDeleted;

    private Boolean myLike;

    @Size(max = 512)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags;
}