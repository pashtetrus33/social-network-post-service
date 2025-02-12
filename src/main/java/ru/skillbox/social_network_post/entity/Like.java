package ru.skillbox.social_network_post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "likes", schema = "schema_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Positive
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;
}
