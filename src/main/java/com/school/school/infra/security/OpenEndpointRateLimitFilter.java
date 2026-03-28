package com.school.school.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OpenEndpointRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_KEY = "POST:/api/v1/auth/login";
    private static final String REFRESH_KEY = "POST:/api/v1/auth/refresh";
    private static final String REGISTER_KEY = "POST:/api/v1/users/register";

    private static final Map<String, RateLimitRule> RULES = Map.of(
            LOGIN_KEY, new RateLimitRule(10, Duration.ofMinutes(1)),
            REFRESH_KEY, new RateLimitRule(20, Duration.ofMinutes(1)),
            REGISTER_KEY, new RateLimitRule(5, Duration.ofMinutes(1))
    );

    private static final long MAX_WINDOW_MILLIS = RULES.values()
            .stream()
            .mapToLong(rule -> rule.window().toMillis())
            .max()
            .orElse(Duration.ofMinutes(1).toMillis());

    private final Map<String, RateCounter> counters = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(0);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String endpointKey = request.getMethod() + ":" + request.getRequestURI();
        RateLimitRule rule = RULES.get(endpointKey);

        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        String rateKey = endpointKey + ":" + clientIp;

        if (!isAllowed(rateKey, rule)) {
            writeRateLimitResponse(response, rule.window());
            return;
        }

        cleanupOldEntriesIfNeeded();
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String rateKey, RateLimitRule rule) {
        long now = System.currentTimeMillis();

        RateCounter counter = counters.compute(rateKey, (key, existing) -> {
            if (existing == null || now - existing.windowStartMs() >= rule.window().toMillis()) {
                return new RateCounter(now, 1);
            }

            return new RateCounter(existing.windowStartMs(), existing.requests() + 1);
        });

        return counter.requests() <= rule.limit();
    }

    private void cleanupOldEntriesIfNeeded() {
        long handled = requestCounter.incrementAndGet();

        if (handled % 200 != 0) {
            return;
        }

        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(entry -> now - entry.getValue().windowStartMs() > (MAX_WINDOW_MILLIS * 2));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response, Duration window) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
        response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Try again later.\"}");
    }

    private record RateLimitRule(int limit, Duration window) {
    }

    private record RateCounter(long windowStartMs, int requests) {
    }
}
