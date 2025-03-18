package ru.skillbox.social_network_post.client;

import feign.Body;
import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AccountSearchDto;

import java.util.List;
import java.util.UUID;

public interface  AccountServiceClient {

    @RequestLine("GET /accountIds?ids={ids}")
    List<AccountDto> getAccountsByIds(@Param("ids") List<UUID> ids);

    @RequestLine("GET /search")
    @Body("%7B\"query\": \"{query}\"%7D")
    List<AccountDto> searchAccount(@Param("query") AccountSearchDto accountSearchDto);
}