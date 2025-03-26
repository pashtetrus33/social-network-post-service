package ru.skillbox.social_network_post.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.security.SecurityUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final AuthServiceClient authServiceClient;
    private final AccountServiceClient accountServiceClient;

    @Scheduled(fixedRate = 300_000) // 5 минут = 300000 мс
    public void executeTask() {
        log.warn("Запущена запланированная задача в {}", System.currentTimeMillis());

        boolean isTokenValid = tokenValidation(SecurityUtils.getToken());

        if (isTokenValid) {
            List<UUID> accountIds = getAccountIds();
            log.warn("Fetch all account ids: {}", accountIds);
        } else {
         log.warn("Token validation failed!!!");
        }
    }

    private boolean tokenValidation(String token) {

        boolean isTokenValid;

        try {
            isTokenValid = authServiceClient.validateToken(token);
            log.info("Scheduled Token valid: {}", isTokenValid);
            return isTokenValid;
        } catch (FeignException e) {
            throw new CustomFreignException("Schedule task. Error trying to validate token");
        } catch (Exception e) {
            log.warn("Schedule task. Unknown error while trying to validate token: {}", e.getMessage());
        }
        return false;
    }


    @LogExecutionTime
    @Retryable(backoff = @Backoff(delay = 2000))
    public List<UUID> getAccountIds() {
        try {
            return accountServiceClient.getAllAccounts(Integer.MAX_VALUE).getContent().stream().map(AccountDto::getId).toList();
        } catch (FeignException e) {
            throw new CustomFreignException("Error fetching all accounts");
        }
    }
}