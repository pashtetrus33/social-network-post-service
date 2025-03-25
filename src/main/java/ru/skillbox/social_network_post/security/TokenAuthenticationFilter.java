package ru.skillbox.social_network_post.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient; // Сервис для валидации токена через Feign
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        int port = request.getLocalPort();

        log.info("Request path: {}", path);
        log.info("Request port: {}", port);

        // Разрешаем доступ к /actuator/** без проверки токена
        if (path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);


        if (authHeader == null) {
            unauthorizedResponse(response, "{\"error\": \"Missing Authorization header\"}");
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            unauthorizedResponse(response, "{\"error\": \"Invalid token format\"}");
            return;
        }

        String token = authHeader.substring(7);

        boolean isTokenValid = authServiceClient.validateToken(token);
        log.info("Token valid: {}", isTokenValid);

        if (!isTokenValid) {
            unauthorizedResponse(response, "{\"error\": \"Invalid or expired token\"}");
            return;
        }

        String userName;
        UUID userId;

        try {
            userName = jwtUtil.extractUsername(token);
            userId = jwtUtil.extractUserId(token);
            log.warn("Username: {}", userName);
            log.warn("UserId: {}", userId);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            unauthorizedResponse(response, e.getMessage());
            return;
        }


        List<String> roles = jwtUtil.extractRoles(token);


        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityUtils.removeToken();
        SecurityUtils.saveToken(token);
        log.warn("Token is saved: {}", token);

        Authentication authentication = new HeaderAuthenticationToken(userId, userName, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.warn("Successfully authenticated user: {}", userName);
        filterChain.doFilter(request, response);
    }

    private void unauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
    }
}