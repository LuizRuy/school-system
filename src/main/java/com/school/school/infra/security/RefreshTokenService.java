package com.school.school.infra.security;

import com.school.school.infra.exception.BusinessException;
import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.repository.RefreshTokenRepository;
import com.school.school.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userService.findByEmail(username);

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
