package ru.skillbox.social_network_post.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AuthenticateRq;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.security.SecurityUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    @Value("${authentication.login}")
    private String login;

    @Value("${authentication.password}")
    private String password;

    private final AuthServiceClient authServiceClient;
    private final AccountServiceClient accountServiceClient;

    @Scheduled(fixedRate = 300_000) // 5 минут = 300000 мс
    public void executeTask() {
        log.warn("Scheduled task.... {}", System.currentTimeMillis());

        boolean isTokenValid = tokenValidation(SecurityUtils.getToken());

        if (isTokenValid) {
            List<UUID> accountIds = getAccountIds();
            log.warn("Scheduled task. Fetch all account ids: {}", accountIds);
        } else {
            log.warn("Scheduled task. Token validation failed!!! Trying to login....");

            String token = authenticateUser(login, password);

            if (token != null) {
                log.info("Scheduled task. Login successful");
                SecurityUtils.saveToken(token);
            }
        }
    }

    private boolean tokenValidation(String token) {

        boolean isTokenValid;

        try {
            isTokenValid = authServiceClient.validateToken(token);
            log.info("Scheduled task. Token valid: {}", isTokenValid);
            return isTokenValid;
        } catch (FeignException e) {
            log.warn("Scheduled task. FreignException trying to validate token");
        } catch (Exception e) {
            log.warn("Scheduled task. Unknown error while trying to validate token: {}", e.getMessage());
        }
        return false;
    }

    private String authenticateUser(String login, String password) {

        String accessToken;

        try {
            AuthenticateRq authenticateRq = new AuthenticateRq();
            authenticateRq.setEmail(login);
            authenticateRq.setPassword(password);

            log.warn("Scheduled task. AuthenticateRq: {}", authenticateRq);

            accessToken = authServiceClient.login(authenticateRq).getAccessToken();

        } catch (FeignException e) {
            log.error("Scheduled task. Freign exception while login with credentials: {} {}", login, password);
            return null;
        }

        return accessToken;
    }


    @LogExecutionTime
    @Retryable(backoff = @Backoff(delay = 2000))
    public List<UUID> getAccountIds() {
        try {
            return accountServiceClient.getAllAccounts(Integer.MAX_VALUE).getContent().stream().map(AccountDto::getId).toList();
        } catch (FeignException e) {
            throw new CustomFreignException("Scheduled task. Error fetching all accounts");
        }
    }
}