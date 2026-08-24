package com.school.school.infra.security;

import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.model.enums.Role;
import com.school.school.repository.RefreshTokenRepository;
import com.school.school.support.MutableClock;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenLifecycleTest {

    private static final long REFRESH_DURATION_MS = 604_800_000L;
    private static final long ACCESS_DURATION_MS = 900_000L;
    private static final String SECRET = "asduighidsomkfldsnam8y8935y3i2hjlkem9sdafjds$!#%^@))(";

    private final MutableClock clock = MutableClock.at("2026-01-01T10:00:00Z");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenLifecycle lifecycle;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(ACCESS_DURATION_MS, SECRET, clock);
        lifecycle = new RefreshTokenLifecycle(refreshTokenRepository, jwtUtil, clock, REFRESH_DURATION_MS);
    }

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setEmail("ada@example.com");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    @DisplayName("issuePair returns an access token carrying the user's claims plus a persisted refresh token")
    void issuePairReturnsDecodableAccessTokenAndPersistedRefreshToken() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IssuedTokens issued = lifecycle.issuePair(user());

        Claims claims = jwtUtil.validateToken(issued.accessToken());
        assertThat(claims.getSubject()).isEqualTo("ada@example.com");
        assertThat(issued.refreshToken()).isNotBlank();
        assertThat(issued.refreshToken()).isNotEqualTo(issued.accessToken());
    }

    @Test
    @DisplayName("issuing a pair first revokes the previous refresh token, then stores the new one")
    void issuingRevokesPreviousRefreshTokenBeforeStoringNewOne() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        lifecycle.issuePair(user());

        InOrder revokeThenIssue = inOrder(refreshTokenRepository);
        revokeThenIssue.verify(refreshTokenRepository).deleteByUser(any(User.class));
        revokeThenIssue.verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("rotate exchanges a live refresh token for a fresh pair")
    void rotateExchangesLiveRefreshTokenForFreshPair() {
        RefreshToken stored = RefreshToken.builder()
                .token("presented-refresh-token")
                .user(user())
                .expiresAt(clock.instant().plusMillis(REFRESH_DURATION_MS))
                .build();
        when(refreshTokenRepository.findByToken("presented-refresh-token")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IssuedTokens issued = lifecycle.rotate("presented-refresh-token");

        assertThat(jwtUtil.validateToken(issued.accessToken()).getSubject()).isEqualTo("ada@example.com");
        assertThat(issued.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("an unknown refresh token fails as invalid, not expired")
    void unknownRefreshTokenIsInvalid() {
        when(refreshTokenRepository.findByToken("never-issued")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lifecycle.rotate("never-issued"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    @DisplayName("an expired refresh token fails as expired relative to the fixed Clock, and is deleted")
    void expiredRefreshTokenIsDistinctFromInvalid() {
        RefreshToken stored = RefreshToken.builder()
                .token("stale-refresh-token")
                .user(user())
                .expiresAt(clock.instant().plusMillis(REFRESH_DURATION_MS))
                .build();
        when(refreshTokenRepository.findByToken("stale-refresh-token")).thenReturn(Optional.of(stored));
        clock.advanceBy(Duration.ofMillis(REFRESH_DURATION_MS).plusSeconds(1));

        assertThatThrownBy(() -> lifecycle.rotate("stale-refresh-token"))
                .isInstanceOf(ExpiredRefreshTokenException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(stored);
    }
}
