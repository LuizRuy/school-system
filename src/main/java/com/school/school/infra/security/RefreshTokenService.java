package com.school.school.infra.security;

import com.school.school.infra.exception.BusinessException;
import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.repository.RefreshTokenRepository;
import com.school.school.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final Clock clock;
    private final long refreshTokenDurationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserService userService,
                               Clock clock,
                               @Value("${jwt.refresh-expiration-ms}") long refreshTokenDurationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
        this.clock = clock;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userService.findByEmail(username);

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now(clock).plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now(clock))) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
