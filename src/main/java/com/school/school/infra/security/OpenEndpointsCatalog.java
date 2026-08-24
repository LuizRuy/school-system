package com.school.school.infra.security;

import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The open endpoints catalog: every public endpoint and its rate limit, in one
 * place, derived from {@link OpenEndpoints#DEFINITIONS} plus optional property
 * overrides ({@link OpenEndpointsProperties}).
 *
 * <p>Consumers:
 * <ul>
 *   <li>{@link SecurityConfig} permits each entry anonymously</li>
 *   <li>{@link OpenEndpointRateLimitFilter} enforces {@link #findRule}</li>
 * </ul>
 */
@Component
public class OpenEndpointsCatalog {

    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    private final List<Entry> entries;
    private final Duration widestWindow;

    @org.springframework.beans.factory.annotation.Autowired
    public OpenEndpointsCatalog(OpenEndpointsProperties properties) {
        this(OpenEndpoints.DEFINITIONS, properties);
    }

    OpenEndpointsCatalog(List<OpenEndpoint> definitions, OpenEndpointsProperties properties) {
        validateOverrides(definitions, properties);
        this.entries = definitions.stream()
                .map(definition -> new Entry(
                        definition,
                        effective(definition, properties),
                        PARSER.parse(definition.pattern())))
                .toList();
        this.widestWindow = entries.stream()
                .map(entry -> entry.rule.window())
                .max(Comparator.naturalOrder())
                .orElse(Duration.ofMinutes(1));
    }

    private static void validateOverrides(List<OpenEndpoint> definitions, OpenEndpointsProperties properties) {
        List<String> knownNames = definitions.stream().map(OpenEndpoint::name).toList();

        List<String> unknown = properties.getLimits().keySet().stream()
                .filter(name -> !knownNames.contains(name))
                .toList();

        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown open-endpoint override name(s) %s; known names: %s".formatted(unknown, knownNames));
        }
    }

    private static OpenEndpointsCatalog.RateLimitRule effective(OpenEndpoint definition,
                                                                OpenEndpointsProperties properties) {
        OpenEndpointsProperties.LimitOverride override = properties.getLimits().get(definition.name());
        if (override == null) {
            return new RateLimitRule(definition.defaultLimit(), definition.defaultWindow());
        }
        return new RateLimitRule(
                override.getLimit() == null ? definition.defaultLimit() : override.getLimit(),
                override.getWindow() == null ? definition.defaultWindow() : override.getWindow());
    }

    /**
     * Every public endpoint with its effective limit/window after overrides.
     * Security configuration derives its anonymous matchers from exactly this list.
     */
    public List<OpenEndpoint> endpoints() {
        return entries.stream().map(Entry::definition).toList();
    }

    /**
     * The rate-limit rule for a request, or empty when the request does not hit
     * a catalog entry. {@code requestUri} is matched as received (this app has
     * no servlet context path); {@code httpMethodName} comparison ignores case.
     */
    public Optional<RateLimitRule> findRule(String httpMethodName, String requestUri) {
        return entries.stream()
                .filter(entry -> entry.matches(httpMethodName, requestUri))
                .map(Entry::rule)
                .findFirst();
    }

    /** Longest window across all rules; rate-limit cleanup uses it as its bound. */
    public Duration widestWindow() {
        return widestWindow;
    }

    /**
     * @param limit maximum requests per client inside one window
     * @param window window length before the counter resets
     */
    public record RateLimitRule(int limit, Duration window) {
    }

    private record Entry(OpenEndpoint definition, RateLimitRule rule, PathPattern pattern) {

        boolean matches(String httpMethodName, String requestUri) {
            if (!methodMatches(httpMethodName)) {
                return false;
            }
            return pattern.matches(PathContainer.parsePath(requestUri));
        }

        private boolean methodMatches(String httpMethodName) {
            return definition.method() == null
                    || definition.method().name().equalsIgnoreCase(httpMethodName);
        }
    }
}
