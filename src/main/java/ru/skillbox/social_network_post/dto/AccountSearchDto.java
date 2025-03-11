package ru.skillbox.social_network_post.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchDto {

    private List<UUID> ids;
    private String author;
    private String firstName;
    private String lastName;
    private LocalDateTime birthDateFrom;
    private LocalDateTime birthDateTo;
    private String city;
    private String country;
    private Boolean isBlocked;
    private Boolean isDeleted;
    private Integer ageTo;
    private Integer ageFrom;
}
