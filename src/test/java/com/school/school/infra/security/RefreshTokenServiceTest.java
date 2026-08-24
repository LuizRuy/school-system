package com.school.school.infra.security;

import com.school.school.infra.exception.BusinessException;
import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.repository.RefreshTokenRepository;
import com.school.school.service.UserService;
import com.school.school.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long DURATION_MS = 604_800_000L;

    private final MutableClock clock = MutableClock.at("2026-01-01T10:00:00Z");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, userService, clock, DURATION_MS);
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("ada@example.com");
        return user;
    }

    @Test
    @DisplayName("createRefreshToken sets expiresAt to Clock now plus duration")
    void createSetsExpiryFromClock() {
        User owner = user();
        when(userService.findByEmail("ada@example.com")).thenReturn(owner);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken token = service.createRefreshToken("ada@example.com");

        assertThat(token.getExpiresAt()).isEqualTo(clock.instant().plusMillis(DURATION_MS));
        verify(refreshTokenRepository).deleteByUser(owner);
    }

    @Test
    @DisplayName("verifyExpiration deletes and rejects a token expired relative to the Clock")
    void expiredTokenIsRejected() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(clock.instant().minusSeconds(1));

        assertThatThrownBy(() -> service.verifyExpiration(token))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    @DisplayName("advancing the clock past the expiry makes a previously valid token expire")
    void advancingClockExpiresToken() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(clock.instant().plusMillis(DURATION_MS));
        assertThat(service.verifyExpiration(token)).isSameAs(token);

        clock.advanceBy(Duration.ofMillis(DURATION_MS).plusSeconds(1));

        assertThatThrownBy(() -> service.verifyExpiration(token))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");
        verify(refreshTokenRepository).delete(token);
    }
}
