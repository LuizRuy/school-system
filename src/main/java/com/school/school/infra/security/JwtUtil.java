package com.school.school.infra.security;

import com.school.school.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final long expiration;
    private final String secret;
    private final Clock clock;

    public JwtUtil(@Value("${jwt.expiration-ms}") long expiration,
                   @Value("${jwt.secret}") String secret,
                   Clock clock) {
        this.expiration = expiration;
        this.secret = secret;
        this.clock = clock;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user){
        Instant now = Instant.now(clock);
        Instant exp = now.plusMillis(expiration);
        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(getSigningKey())
                .compact();

        return token;

    }

    public Claims validateToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setClock(() -> Date.from(Instant.now(clock)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
