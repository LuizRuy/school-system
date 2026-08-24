package com.school.school.infra.security;

import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.model.enums.Role;
import com.school.school.repository.RefreshTokenRepository;
import com.school.school.repository.UserRepository;
import com.school.school.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({RefreshTokenLifecycle.class,
         RefreshTokenLifecycleRotationTest.FixedClockConfig.class})
class RefreshTokenLifecycleRotationTest {

    private static final Instant BASE = Instant.parse("2026-01-01T10:00:00Z");
    private static final long REFRESH_DURATION_MS = 604_800_000L;
    private static final long ACCESS_DURATION_MS = 900_000L;
    private static final String SECRET = "asduighidsomkfldsnam8y8935y3i2hjlkem9sdafjds$!#%^@))(";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private TokenLifecycle tokenLifecycle;

    @BeforeEach
    void resetClock() {
        FixedClockConfig.CLOCK.setTo(BASE);
    }

    @TestConfiguration
    static class FixedClockConfig {

        static final MutableClock CLOCK = MutableClock.at(BASE.toString());

        @Bean
        Clock clock() {
            return CLOCK;
        }

        @Bean
        JwtUtil jwtUtil() {
            return new JwtUtil(ACCESS_DURATION_MS, SECRET, CLOCK);
        }
    }

    private User newUser() {
        return User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada-%s@example.com".formatted(UUID.randomUUID()))
                .password("hashed")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("issuing a pair persists exactly one refresh token expiring at Clock-now plus duration")
    void issuedRefreshTokenIsStoredWithClockBasedExpiry() {
        User user = userRepository.saveAndFlush(newUser());

        IssuedTokens issued = tokenLifecycle.issuePair(user);

        RefreshToken stored = refreshTokenRepository.findByToken(issued.refreshToken()).orElseThrow();
        assertThat(stored.getUser().getId()).isEqualTo(user.getId());
        assertThat(stored.getExpiresAt()).isEqualTo(BASE.plusMillis(REFRESH_DURATION_MS));
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("a new login invalidates the previous refresh token")
    void reLoginInvalidatesPreviousRefreshToken() {
        User user = userRepository.saveAndFlush(newUser());

        String firstLoginRefresh = tokenLifecycle.issuePair(user).refreshToken();
        String secondLoginRefresh = tokenLifecycle.issuePair(user).refreshToken();

        assertThatThrownBy(() -> tokenLifecycle.rotate(firstLoginRefresh))
                .isInstanceOf(InvalidRefreshTokenException.class);

        IssuedTokens rotated = tokenLifecycle.rotate(secondLoginRefresh);
        assertThat(rotated.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("reusing an already-rotated refresh token fails; only the newest token rotates")
    void rotatedRefreshTokenCannotBeReused() {
        User user = userRepository.saveAndFlush(newUser());

        String original = tokenLifecycle.issuePair(user).refreshToken();
        String firstRotation = tokenLifecycle.rotate(original).refreshToken();

        assertThatThrownBy(() -> tokenLifecycle.rotate(original))
                .isInstanceOf(InvalidRefreshTokenException.class);

        IssuedTokens secondRotation = tokenLifecycle.rotate(firstRotation);
        assertThat(secondRotation.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("advancing the fixed Clock past the stored expiry makes rotation fail as expired and deletes the row")
    void expiredStoredRefreshTokenFailsAsExpiredAndIsDeleted() {
        User user = userRepository.saveAndFlush(newUser());

        String raw = tokenLifecycle.issuePair(user).refreshToken();

        FixedClockConfig.CLOCK.advanceBy(Duration.ofMillis(REFRESH_DURATION_MS).plusSeconds(1));

        assertThatThrownBy(() -> tokenLifecycle.rotate(raw))
                .isInstanceOf(ExpiredRefreshTokenException.class)
                .hasMessageContaining("expired");

        assertThat(refreshTokenRepository.findByToken(raw)).isEmpty();
    }
}
