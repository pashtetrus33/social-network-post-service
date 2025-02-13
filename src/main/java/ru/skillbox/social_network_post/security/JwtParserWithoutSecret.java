package ru.skillbox.social_network_post.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtParserWithoutSecret {

    public static void parse(String jwt) {

        Claims claims = Jwts.parser()
                .setSigningKey("FAKE_SECRET_KEY")
                .parseClaimsJws(jwt)
                .getBody();

        System.out.println("Payload: " + claims);
    }
}