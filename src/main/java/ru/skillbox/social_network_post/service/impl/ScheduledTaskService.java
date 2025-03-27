package ru.skillbox.social_network_post.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.aspect.LogExecutionTime;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AuthenticateRq;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.TagDto;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.security.HeaderAuthenticationToken;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.PostService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    @Value("${authentication.login}")
    private String login;

    @Value("${authentication.password}")
    private String password;

    @Value("${publish-date.after}")
    private String publishDateAfterNow;

    @Value("${publish-date.before}")
    private String publishDateBeforeNow;

    private final AuthServiceClient authServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final PostService postService;
    private final RandomQuoteGenerator randomQuoteGenerator;

    private static final AtomicInteger counter = new AtomicInteger(1);

    @Scheduled(fixedRate = 1_800_000) // 30 мин = 1 800 000 мс
    public void executeTask() {
        log.warn("Scheduled task.... {}", System.currentTimeMillis());

        boolean isTokenValid = tokenValidation(SecurityUtils.getToken());

        if (!isTokenValid) {
            log.warn("Scheduled task. Token validation failed!!! Trying to login....");

            String token = authenticateUser(login, password);

            if (token != null) {
                log.info("Scheduled task. Login successful");
                SecurityUtils.saveToken(token);
            } else {
                log.warn("Scheduled task. Token null");
            }
        }

        List<UUID> accountIds = getAccountIds();
        log.warn("Scheduled task. Fetch all account ids: {}", accountIds);

        List<UUID> mutableList = new ArrayList<>(accountIds);
        Collections.shuffle(mutableList);

        mutableList.forEach(accountId -> {
            try {

                String userName = "Scheduled_user";

                Authentication authentication = new HeaderAuthenticationToken(accountId, userName,
                        Collections.singletonList(new SimpleGrantedAuthority("Scheduled_User")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.warn("Successfully authenticated user: {}", accountId);

                // Искусственная задержка 1-2 секунды между запросами цитат
                Thread.sleep(1500 + ThreadLocalRandom.current().nextLong(1000));
                log.warn("Город засыпает.... Просыпается мафия ಠ_ಠ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<String> quoteResult = randomQuoteGenerator.getRandomQuote();

            postService.create(PostDto.builder()
                    .title("Цитата дня #" + counter.getAndIncrement())
                    .postText("\"" + quoteResult.get(1) + "\" — " + quoteResult.get(0))
                    .tags(List.of(new TagDto(quoteResult.get(0)), new TagDto("цитата")))
                    .publishDate(createRandomPublishDate())
                    .authorId(accountId)
                    .build());
        });
    }


    private LocalDateTime createRandomPublishDate() {

        LocalDateTime now = LocalDateTime.now();

        try {

            long before = Long.parseLong(publishDateBeforeNow);
            long after = Long.parseLong(publishDateAfterNow);

            if (before < 0 || after < 0) {
                throw new IllegalArgumentException("publish-date.before и publish-date.after должны быть неотрицательными");
            }

            LocalDateTime startDate = now.minusDays(before);
            LocalDateTime endDate = now.plusDays(after);

            Duration duration = Duration.between(startDate, endDate);
            long secondsBetween = duration.getSeconds();

            long randomSeconds = ThreadLocalRandom.current().nextLong(secondsBetween);

            LocalDateTime randomDate = startDate.plusSeconds(randomSeconds);
            log.info("Случайная дата публикации: {}", randomDate);


            return randomDate;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка преобразования publish-date.before или publish-date.after в число", e);
        }
    }

    boolean tokenValidation(String token) {

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

    String authenticateUser(String login, String password) {

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
            return accountServiceClient.getAllAccounts().getContent().stream().map(AccountDto::getId).toList();
        } catch (FeignException e) {
            throw new CustomFreignException("Scheduled task. Error fetching all accounts");
        }
    }
}