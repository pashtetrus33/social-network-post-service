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

    private List<@PositiveOrZero(message = "Each ID must be a positive number or zero") Long> ids;
    private List<@PositiveOrZero(message = "Each account ID must be a positive number or zero") Long> accountIds;
    private List<@PositiveOrZero(message = "Each blocked ID must be a positive number or zero") Long> blockedIds;

    @Size(max = 255, message = "Author name must not exceed 255 characters")
    private String author;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String postText;

    private Boolean withFriends;

    private Boolean isDelete;

    @Size(max = 50, message = "Tags list size must not exceed 50")
    private List<String> tags;

    @PositiveOrZero(message = "Date from must be zero or a positive number")
    private Long dateFrom;

    @PositiveOrZero(message = "Date to must be zero or a positive number")
    private Long dateTo;
}