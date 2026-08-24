package com.school.school.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enforces the per-client rate limits declared by {@link OpenEndpointsCatalog}
 * on the public endpoints. Which requests are limited — and with which limit —
 * is decided entirely by the catalog; this filter only counts and blocks.
 */
@Component
public class OpenEndpointRateLimitFilter extends OncePerRequestFilter {

    private static final int CLEANUP_EVERY = 200;

    private final Clock clock;
    private final OpenEndpointsCatalog catalog;
    private final ClientIpResolver clientIpResolver;
    private final Map<String, RateCounter> counters = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(0);

    public OpenEndpointRateLimitFilter(Clock clock,
                                       OpenEndpointsCatalog catalog,
                                       ClientIpResolver clientIpResolver) {
        this.clock = clock;
        this.catalog = catalog;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String endpointKey = request.getMethod() + ":" + request.getRequestURI();

        var rule = catalog.findRule(request.getMethod(), request.getRequestURI()).orElse(null);

        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String rateKey = endpointKey + ":" + clientIpResolver.resolve(request);

        if (!isAllowed(rateKey, rule)) {
            writeRateLimitResponse(response, rule.window());
            return;
        }

        cleanupOldEntriesIfNeeded();
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String rateKey, OpenEndpointsCatalog.RateLimitRule rule) {
        long now = clock.millis();

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

        if (handled % CLEANUP_EVERY != 0) {
            return;
        }

        long now = clock.millis();
        long maxWindowMillis = catalog.widestWindow().toMillis();
        counters.entrySet().removeIf(entry -> now - entry.getValue().windowStartMs() > (maxWindowMillis * 2));
    }

    private void writeRateLimitResponse(HttpServletResponse response, Duration window) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
        response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Try again later.\"}");
    }

    private record RateCounter(long windowStartMs, int requests) {
    }
}
