package ru.skillbox.social_network_post.client;

import feign.Body;
import feign.Param;
import feign.RequestLine;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AccountSearchDto;

import java.util.List;
import java.util.UUID;

public interface AccountServiceClient {


    // GET запрос для получения аккаунтов по списку идентификаторов
    @RequestLine("GET /accountIds?ids={ids}")
    List<AccountDto> getAccountsByIds(@Param("ids") List<UUID> ids);

    @RequestLine("POST /search")
    @Body("{{accountSearchDto}}") // Сериализация объекта AccountSearchDto в тело запроса
    List<AccountDto> searchAccount(AccountSearchDto accountSearchDto);
}