package ru.skillbox.social_network_post.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class XUserIdAuthenticationToken extends AbstractAuthenticationToken {
    private final String userId;

    public XUserIdAuthenticationToken(String userId) {
        super(null);
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
