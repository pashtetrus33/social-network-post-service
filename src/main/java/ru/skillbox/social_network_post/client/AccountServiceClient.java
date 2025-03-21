package ru.skillbox.social_network_post.client;

import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_post.dto.AccountDto;

import java.util.List;
import java.util.UUID;

public interface  AccountServiceClient {

    @RequestLine("GET /accountIds?ids={ids}")
    List<AccountDto> getAccountsByIds(@Param("ids") List<UUID> ids);

    @RequestLine("GET /search?author={author}")
    List<AccountDto> searchAccount(@Param("author") String author);
}