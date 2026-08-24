package com.school.school.infra.security;

import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.List;

/**
 * The single place where every public (unauthenticated) endpoint is declared.
 *
 * <p>To publish and rate-limit a new endpoint, add one line here. Nothing else
 * needs editing: {@link SecurityConfig} permits the pattern anonymously and
 * {@link OpenEndpointRateLimitFilter} enforces the rule.
 *
 * <p>Limits and windows can be tuned at runtime via application properties,
 * keyed by entry name:
 * <pre>
 * school.open-endpoints.limits.login.limit=15
 * school.open-endpoints.limits.register.window=PT2M
 * </pre>
 */
public final class OpenEndpoints {

    /**
     * Every public endpoint with its default rate limit. Order matters only if
     * patterns overlap; the first matching entry wins.
     */
    public static final List<OpenEndpoint> DEFINITIONS = List.of(
            new OpenEndpoint("login", HttpMethod.POST, "/api/v1/auth/login", 10, Duration.ofMinutes(1)),
            new OpenEndpoint("refresh", HttpMethod.POST, "/api/v1/auth/refresh", 20, Duration.ofMinutes(1)),
            new OpenEndpoint("register", HttpMethod.POST, "/api/v1/users/register", 5, Duration.ofMinutes(1)),
            new OpenEndpoint("swagger-ui", null, "/swagger-ui/**", 300, Duration.ofMinutes(1)),
            new OpenEndpoint("swagger-ui-html", null, "/swagger-ui.html", 300, Duration.ofMinutes(1)),
            new OpenEndpoint("api-docs", null, "/v3/api-docs/**", 300, Duration.ofMinutes(1))
    );

    private OpenEndpoints() {
    }
}
