package ru.skillbox.social_network_post.web.model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchDto {

    private List<@Positive Long> ids;
    private List<@Positive Long> accountIds;
    private List<@Positive Long> blockedIds;

    @Size(max = 255)
    private String author;

    @Size(max = 255)
    private String title;

    private String postText;

    private Boolean withFriends;

    private Boolean isDelete;

    @Size(max = 50)
    private List<String> tags;

    @PositiveOrZero
    private Long dateFrom;

    @PositiveOrZero
    private Long dateTo;
}