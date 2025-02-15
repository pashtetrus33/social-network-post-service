package ru.skillbox.social_network_post.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AccountSearchDto;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountServiceClient {
    @GetMapping("/accountIds")
    List<AccountDto> getAccountsByIds(@RequestParam List<UUID> ids);

    @PostMapping("/searchByFilter")
    UUID getAccountByName(@RequestBody AccountSearchDto name);
}