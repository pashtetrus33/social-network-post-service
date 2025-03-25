package ru.skillbox.social_network_post.service;

import feign.FeignException;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.exception.CustomFreignException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final AccountServiceClient accountServiceClient;

    @Scheduled(fixedRate = 1_800_000) // 30 минут = 1800000 мс
    public void executeTask() {
        log.info("Запущена запланированная задача в {}", System.currentTimeMillis());
        List<UUID> accountIds = getAccountIds();
        accountIds.forEach(accountId -> log.warn(accountId.toString()));
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