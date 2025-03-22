package ru.skillbox.social_network_post.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.skillbox.social_network_post.client.AuthServiceClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TokenAuthenticationFilterTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testDoFilterInternal_WhenTokenMissing_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Simulate request with no Authorization header
        request.setServletPath("/some/path");

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify that response status is set to 401 Unauthorized
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testDoFilterInternal_WhenTokenInvalid_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Simulate request with invalid token
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid_token");

        when(authServiceClient.validateToken(anyString())).thenReturn(false);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify that response status is set to 401 Unauthorized
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testDoFilterInternal_WhenTokenValid_ShouldAuthenticateUser() throws ServletException, IOException {
        // Simulate request with valid token
        String validToken = "valid_token";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);

        when(authServiceClient.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("testUser");
        when(jwtUtil.extractUserId(validToken)).thenReturn(UUID.randomUUID());
        when(jwtUtil.extractRoles(validToken)).thenReturn(List.of("ROLE_USER"));

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        // Ensure that the authentication was set correctly
        assertNotNull(authentication);
        assertEquals("testUser", authentication.getName());
    }
}