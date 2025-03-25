package ru.skillbox.social_network_post.client;

import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_post.dto.PageAccountDto;

import java.util.List;
import java.util.UUID;

public interface  AccountServiceClient {

    @RequestLine("GET /accountIds?ids={ids}")
    PageAccountDto getAccountsByIds(@Param("ids") List<UUID> ids);

    @RequestLine("GET /search?author={author}")
    PageAccountDto searchAccount(@Param("author") String author);

    @RequestLine("GET /?page={page}&size={size}&sort={sort}")
    PageAccountDto getAllAccounts(@Param("size") int size);
}