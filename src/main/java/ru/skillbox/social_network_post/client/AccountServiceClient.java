package ru.skillbox.social_network_post.client;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AccountSearchDto;

import java.util.List;
import java.util.UUID;

public interface AccountServiceClient {

    //@GetMapping("/accountIds")
    //List<AccountDto> getAccountsByIds(@RequestParam List<UUID> ids);

    @PostMapping("/searchByFilter")
    List<AccountDto> searchAccount(@RequestBody AccountSearchDto accountSearchDto);
}