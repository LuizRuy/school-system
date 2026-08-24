package com.school.school.infra.security;

import com.school.school.model.User;
import com.school.school.model.enums.Role;
import com.school.school.support.MutableClock;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final long EXPIRATION_MS = 60_000L;
    private static final String SECRET = "asduighidsomkfldsnam8y8935y3i2hjlkem9sdafjds$!#%^@))(";

    private final MutableClock clock = MutableClock.at("2026-01-01T10:00:00Z");
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(EXPIRATION_MS, SECRET, clock);
    }

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setEmail("ada@example.com");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    @DisplayName("issued-at is read from the injected Clock")
    void issuedAtComesFromClock() {
        Claims claims = jwtUtil.validateToken(jwtUtil.generateToken(user()));

        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(clock.instant()));
        assertThat(claims.getSubject()).isEqualTo("ada@example.com");
    }

    @Test
    @DisplayName("expiration is issued-at plus the configured duration")
    void expirationIsIssuedAtPlusDuration() {
        Claims claims = jwtUtil.validateToken(jwtUtil.generateToken(user()));

        assertThat(claims.getExpiration()).isEqualTo(Date.from(clock.instant().plusMillis(EXPIRATION_MS)));
    }

    @Test
    @DisplayName("advancing the clock shifts the token timestamps deterministically")
    void advancingClockShiftsTimestamps() {
        clock.advanceBy(Duration.ofMinutes(30));

        Claims claims = jwtUtil.validateToken(jwtUtil.generateToken(user()));

        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(clock.instant()));
        assertThat(claims.getExpiration()).isEqualTo(Date.from(clock.instant().plusMillis(EXPIRATION_MS)));
    }
}
