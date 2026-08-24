package com.school.school.service;

import com.school.school.infra.exception.BusinessException;
import com.school.school.infra.security.CustomUserDetailsService;
import com.school.school.infra.security.JwtUtil;
import com.school.school.infra.security.RefreshTokenLifecycle;
import com.school.school.mapper.AuthMapper;
import com.school.school.model.User;
import com.school.school.model.dto.auth.AuthRequest;
import com.school.school.model.dto.auth.AuthResponse;
import com.school.school.model.enums.Role;
import com.school.school.model.enums.Status;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({CustomUserDetailsService.class,
         AuthMapper.class,
         RefreshTokenLifecycle.class,
         AuthService.class,
         LoginSingleUserQueryTest.LoginWiring.class})
class LoginSingleUserQueryTest {

    private static final Instant BASE = Instant.parse("2026-01-01T10:00:00Z");
    private static final String RAW_PASSWORD = "password123";
    private static final long ACCESS_DURATION_MS = 900_000L;
    private static final long REFRESH_DURATION_MS = 604_800_000L;
    private static final String SECRET = "asduighidsomkfldsnam8y8935y3i2hjlkem9sdafjds$!#%^@))(";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private UserRepository spiedUserRepository;

    @BeforeEach
    void resetClock() {
        LoginWiring.CLOCK.setTo(BASE);
    }

    @TestConfiguration
    static class LoginWiring {

        static final MutableClock CLOCK = MutableClock.at(BASE.toString());

        @Bean
        Clock clock() {
            return CLOCK;
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        JwtUtil jwtUtil() {
            return new JwtUtil(ACCESS_DURATION_MS, SECRET, CLOCK);
        }

        @Bean
        AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService,
                                                    PasswordEncoder encoder) {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
            provider.setPasswordEncoder(encoder);
            return new ProviderManager(provider);
        }
    }

    @Test
    @DisplayName("login performs exactly one user query and returns a working token pair")
    void loginIssuesTokensWithExactlyOneUserQuery() {
        User user = persistedEnabledUser();

        AuthResponse response = authService.login(requestFor(user));

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getToken()).isNotBlank();

        verify(spiedUserRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("a disabled user is rejected with the business error after the single lookup")
    void disabledUserIsRejectedAfterSingleLookup() {
        User user = persistedEnabledUser();
        user.setStatus(Status.DISABLED);
        userRepository.saveAndFlush(user);

        assertThatThrownBy(() -> authService.login(requestFor(user)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getMessage()).contains("disabled"));

        verify(spiedUserRepository, times(1)).findByEmail(anyString());
    }

    private AuthRequest requestFor(User user) {
        AuthRequest request = new AuthRequest();
        request.setEmail(user.getEmail());
        request.setPassword(RAW_PASSWORD);
        return request;
    }

    private User persistedEnabledUser() {
        return userRepository.saveAndFlush(User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada-%s@example.com".formatted(UUID.randomUUID()))
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .role(Role.USER)
                .status(Status.ENABLED)
                .build());
    }
}
