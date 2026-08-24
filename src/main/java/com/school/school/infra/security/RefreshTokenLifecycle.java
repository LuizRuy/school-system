package com.school.school.infra.security;

import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenLifecycle implements TokenLifecycle {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final Clock clock;
    private final long refreshTokenDurationMs;

    public RefreshTokenLifecycle(RefreshTokenRepository refreshTokenRepository,
                                 JwtUtil jwtUtil,
                                 Clock clock,
                                 @Value("${jwt.refresh-expiration-ms}") long refreshTokenDurationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.clock = clock;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    @Override
    @Transactional
    public IssuedTokens issuePair(User user) {
        return freshPairFor(user);
    }

    @Override
    @Transactional
    public IssuedTokens rotate(String rawRefreshToken) {
        RefreshToken presented = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        deleteAndRejectIfExpired(presented);

        return freshPairFor(presented.getUser());
    }

    @Override
    public Claims validateAccess(String rawAccessToken) {
        return jwtUtil.validateToken(rawAccessToken);
    }

    private IssuedTokens freshPairFor(User user) {
        revokeActiveTokens(user);
        return new IssuedTokens(jwtUtil.generateToken(user), mintAndStore(user));
    }

    private void revokeActiveTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
    }

    private void deleteAndRejectIfExpired(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now(clock))) {
            refreshTokenRepository.delete(token);
            throw new ExpiredRefreshTokenException();
        }
    }

    private String mintAndStore(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now(clock).plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }
}
