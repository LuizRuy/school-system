package com.school.school.infra.security;

import org.springframework.http.HttpMethod;

import java.time.Duration;

/**
 * One entry in the open endpoints catalog: an endpoint reachable without
 * authentication, together with its per-client rate limit.
 *
 * <p>Adding an entry to {@link OpenEndpoints#DEFINITIONS} is all that is needed
 * to make a new endpoint public and rate-limited; both {@link SecurityConfig}
 * and {@link OpenEndpointRateLimitFilter} derive their behaviour from this list.
 *
 * @param name         stable key used in configuration overrides
 *                     ({@code school.open-endpoints.limits.<name>.*}); lowercase kebab-case
 * @param method       the HTTP method the entry applies to, or null for any method
 * @param pattern      path pattern; exact segments or {@code **} wildcards
 * @param defaultLimit maximum requests per client inside one window
 * @param defaultWindow window length; overridable via properties
 */
public record OpenEndpoint(String name, HttpMethod method, String pattern, int defaultLimit, Duration defaultWindow) {
}
