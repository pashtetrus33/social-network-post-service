package ru.skillbox.social_network_post.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {

    @NotBlank(message = "Image path must not be blank")
    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;
}