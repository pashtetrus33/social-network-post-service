package ru.skillbox.social_network_post.client;

import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_post.dto.PageAccountDto;

public interface  AccountServiceClient {

    @RequestLine("GET /search?author={author}")
    PageAccountDto searchAccount(@Param("author") String author);

    @RequestLine("GET")
    PageAccountDto getAllAccounts();
}