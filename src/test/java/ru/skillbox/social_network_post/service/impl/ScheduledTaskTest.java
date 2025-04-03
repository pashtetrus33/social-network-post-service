package ru.skillbox.social_network_post.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import ru.skillbox.social_network_post.dto.*;
import ru.skillbox.social_network_post.exception.CustomFreignException;
import ru.skillbox.social_network_post.security.SecurityUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduledTaskTest extends AbstractTest {

    @MockBean
    private PostServiceImpl postService;

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
    }


    @Test
    void testTokenValidationSuccess() {
        // Arrange
        String validToken = "valid-token";
        when(authServiceClient.validateToken(validToken)).thenReturn(true);

        // Act
        boolean isValid = scheduledTaskService.tokenValidation(validToken);

        // Assert
        assertTrue(isValid, "Token should be valid");
    }


    @Test
    void testTokenValidationFailure() {
        // Arrange
        String invalidToken = "invalid-token";
        when(authServiceClient.validateToken(invalidToken)).thenReturn(false);

        // Act
        boolean isValid = scheduledTaskService.tokenValidation(invalidToken);

        // Assert
        assertFalse(isValid, "Token should be invalid");
    }


    @Test
    void testAuthenticateUserSuccess() {
        // Arrange
        String login = "test@example.com";
        String password = "password";

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("084de31a-cbfc-423a-b41b-b5c08199c110");

        when(authServiceClient.login(any(AuthenticateRq.class))).thenReturn(tokenResponse);

        // Act
        String token = scheduledTaskService.authenticateUser(login, password);

        // Assert
        assertNotNull(token, "Token should not be null");
        assertEquals("084de31a-cbfc-423a-b41b-b5c08199c110", token, "Token should match expected");
    }


    @Test
    void testGetAccountIds() {
        // Arrange
        PageAccountDto pageAccountDto = PageAccountDto.builder()
                .content(Collections.singletonList(AccountDto.builder()
                        .id(UUID.fromString("084de31a-cbfc-423a-b41b-b5c08199c110"))
                        .build()))
                .build();

        when(accountServiceClient.getAllAccounts()).thenReturn(pageAccountDto); // AccountResponse - класс-обертка

        // Act
        List<UUID> accountIds = scheduledTaskService.getAccountIds();

        // Assert
        assertEquals(1, accountIds.size(), "Account list should have one element");
        assertEquals(UUID.fromString("084de31a-cbfc-423a-b41b-b5c08199c110"), accountIds.get(0), "Account ID should match");
    }


    @Test
    void testGetAccountIdsFailure() {
        // Arrange
        when(accountServiceClient.getAllAccounts()).thenThrow(new CustomFreignException("Error fetching all accounts"));

        // Act & Assert
        assertThrows(CustomFreignException.class, scheduledTaskService::getAccountIds, "Exception should be thrown");
    }


    @Test
    void testTokenValidationWithFeignException() {
        // Arrange
        String invalidToken = "invalid-token";
        when(authServiceClient.validateToken(invalidToken)).thenThrow(new CustomFreignException("Token validation failed"));

        // Act & Assert
        assertFalse(scheduledTaskService.tokenValidation(invalidToken), "Token validation should fail when FeignException occurs");
    }


    @Test
    void testAuthenticateUserFailure() {
        // Arrange
        String login = "test@example.com";
        String password = "password";

        when(authServiceClient.login(any(AuthenticateRq.class)))
                .thenThrow(new CustomFreignException("Authentication failed"));

        // Act & Assert
        assertThrows(CustomFreignException.class, () -> scheduledTaskService.authenticateUser(login, password),
                "Authentication should fail with FeignException");
    }


    @Test
    void testGetAccountIdsEmptyList() {
        // Arrange
        when(accountServiceClient.getAllAccounts()).thenReturn(PageAccountDto.builder().content(Collections.emptyList()).build());

        // Act
        List<UUID> accountIds = scheduledTaskService.getAccountIds();

        // Assert
        assertTrue(accountIds.isEmpty(), "Account list should be empty");
    }


    @Test
    void testGetAccountIdsThrowsCustomFreignException() {
        // Arrange
        when(accountServiceClient.getAllAccounts()).thenThrow(new CustomFreignException("Error fetching accounts"));

        // Act & Assert
        assertThrows(CustomFreignException.class, () -> scheduledTaskService.getAccountIds(), "CustomFreignException should be thrown");
    }


    @Test
    void testExecuteTask() {
        // Arrange
        PageAccountDto pageAccountDto = PageAccountDto.builder()
                .content(Collections.singletonList(AccountDto.builder()
                        .build()))
                .build();

        SecurityUtils.saveToken("084de31a-cbfc-423a-b41b-b5c08199c110");

        when(accountServiceClient.getAllAccounts()).thenReturn(pageAccountDto);
        when(authServiceClient.validateToken(anyString())).thenReturn(true);


        // Act
        scheduledTaskService.executeTask();

        // Assert
        verify(postService, atLeastOnce()).create(any(PostDto.class));
    }
}