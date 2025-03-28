package ru.skillbox.social_network_post.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.security.HeaderAuthenticationToken;
import ru.skillbox.social_network_post.security.SecurityUtils;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.service.PostService;
import ru.skillbox.social_network_post.service.ReactionService;
import ru.skillbox.social_network_post.utils.CommentUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final RandomQuoteGenerator randomQuoteGenerator;

    private static final AtomicInteger counter = new AtomicInteger(1);
    private final Random random = new Random();

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
                Thread.sleep(2000L + random.nextInt(1000));
                log.warn("Город засыпает.... Просыпается мафия ಠ_ಠ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<String> quoteResult = randomQuoteGenerator.getRandomQuote();

            List<TagDto> tags = new ArrayList<>();
            tags.add(new TagDto("цитата"));

            if (!StringUtils.isBlank(quoteResult.get(0))) {
                tags.add(new TagDto(quoteResult.get(0)));
            }

            postService.create(PostDto.builder()
                    .title("Цитата дня #" + counter.getAndIncrement())
                    .postText("\"" + quoteResult.get(1) + "\" — " + quoteResult.get(0))
                    .tags(tags)
                    .publishDate(createRandomPublishDate())
                    .authorId(accountId)
                    .build());

            PagePostDto postsForComments = postService.getAll(new PostSearchDto(), PageRequest.of(0, 20, Sort.by(Sort.Order.desc("publishDate"))));

            createFakeComments(postsForComments);
        });
    }

    private void createFakeComments(PagePostDto postsForComments) {

        if (postsForComments != null) {
            log.warn("Got some posts for comments:");
            postsForComments.getContent().forEach(post -> log.warn("Post: {}", post.getId()));

            List<PostDto> postDtos = new ArrayList<>(postsForComments.getContent());
            Collections.shuffle(postDtos);
            postDtos.stream().limit(5).forEach(post -> {

                LocalDateTime publishDate = post.getPublishDate();

                long randomMinutes = 10 + random.nextLong(110);

                LocalDateTime commentTime = publishDate.plusMinutes(randomMinutes);

                log.info("Random publish date for comment: {}", commentTime);

                int postLength = post.getPostText().length();
                int commentLength = (int) (postLength * 0.3);

                String postCut = " " + post.getPostText().substring(0, commentLength) + " ...";

                CommentDto commentDto = CommentDto.builder()
                        .commentText(CommentUtils.getRandomComment(postCut))
                        .time(commentTime)
                        .build();

                commentService.create(post.getId(), commentDto);
                log.warn("Created comment for post {}. {}", post.getId(), commentDto.getCommentText());

                PageCommentDto commentsForComments = commentService.getByPostId(post.getId(), new CommentSearchDto(), PageRequest.of(0, 10, Sort.by(Sort.Order.desc("time"))));

                createReactionsForPosts(post.getId());

                if (commentsForComments != null) {
                    log.warn("Got some comments for comments:");
                    createFakeSubcomments(commentsForComments, post.getId());
                } else {
                    log.warn("Comment not found for post: {}", post.getId());
                }
            });

        } else {
            log.warn("Got no posts for comments");
        }
    }

    private void createReactionsForPosts(UUID postId) {

        RequestReactionDto reaction = RequestReactionDto.builder()
                .type("POST")
                .reactionType(getRandomReaction())
                .build();
        reactionService.addLikeToPost(postId, reaction);

        log.warn("Got some reactions for post: {} Reaction: {}", postId, reaction);
    }


    private String getRandomReaction() {
        ReactionType[] values = ReactionType.values();
        return values[random.nextInt(values.length)].getName();
    }


    private void createFakeSubcomments(PageCommentDto commentsForComments, UUID postId) {

        commentsForComments.getContent().forEach(commentDto -> log.warn("Comment: {}", commentDto.getId()));

        List<CommentDto> commentDtos = new ArrayList<>(commentsForComments.getContent());
        Collections.shuffle(commentDtos);
        commentDtos.stream().limit(5).forEach(commentDto -> {

            LocalDateTime time = commentDto.getTime();

            long randomMinutes = 10 + random.nextLong(110);

            LocalDateTime subCommentTime = time.plusMinutes(randomMinutes);

            log.info("Random subcomment publish date: {}", subCommentTime);

            CommentDto subCommentDto = CommentDto.builder()
                    .commentText(CommentUtils.getRandomReply())
                    .parentId(commentDto.getId())
                    .time(subCommentTime)
                    .build();

            commentService.create(postId, subCommentDto);
            log.warn("Created subcomment {}. {}", postId, commentDto.getCommentText());

            log.warn("Put like to comment {}", commentDto.getId());
            reactionService.addLikeToComment(postId, commentDto.getId());
        });
    }


    private LocalDateTime createRandomPublishDate() {

        LocalDateTime now = LocalDateTime.now();

        try {

            long before = Long.parseLong(publishDateBeforeNow);
            long after = Long.parseLong(publishDateAfterNow);

            if (before < 0 || after < 0) {
                throw new IllegalArgumentException("publish-date.before and publish-date.after must be positive");
            }

            LocalDateTime startDate = now.minusDays(before);
            LocalDateTime endDate = now.plusDays(after);

            Duration duration = Duration.between(startDate, endDate);
            long secondsBetween = duration.getSeconds();

            long randomSeconds = random.nextInt((int) secondsBetween);

            LocalDateTime randomDate = startDate.plusSeconds(randomSeconds);
            log.warn("Random publish date for post: {}", randomDate);


            return randomDate;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parsing publish-date.before or publish-date.after in number", e);
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