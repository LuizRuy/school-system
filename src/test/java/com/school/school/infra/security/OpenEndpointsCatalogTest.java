package com.school.school.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenEndpointsCatalogTest {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private OpenEndpointsCatalog catalogFor(List<OpenEndpoint> definitions, OpenEndpointsProperties properties) {
        return new OpenEndpointsCatalog(definitions, properties);
    }

    private OpenEndpointsCatalog productionCatalog(OpenEndpointsProperties properties) {
        return catalogFor(OpenEndpoints.DEFINITIONS, properties);
    }

    @Test
    @DisplayName("every definition carries its configured default limit and window")
    void everyDefinitionCarriesItsDefaultRule() {
        OpenEndpointsCatalog catalog = productionCatalog(new OpenEndpointsProperties());

        for (OpenEndpoint definition : OpenEndpoints.DEFINITIONS) {
            String method = definition.method() == null ? "GET" : definition.method().name();
            String probeUri = definition.pattern().contains("*")
                    ? probeUriFor(definition.pattern())
                    : definition.pattern();

            assertThat(catalog.findRule(method, probeUri))
                    .as("definition %s", definition.name())
                    .hasValueSatisfying(rule -> {
                        assertThat(rule.limit()).isEqualTo(definition.defaultLimit());
                        assertThat(rule.window()).isEqualTo(definition.defaultWindow());
                    });
        }
    }

    private String probeUriFor(String pattern) {
        return pattern.replace("**", "probe");
    }

    @Test
    @DisplayName("register defaults to five requests per minute")
    void registerDefaultsToFivePerMinute() {
        OpenEndpointsCatalog catalog = productionCatalog(new OpenEndpointsProperties());

        assertThat(catalog.findRule("POST", "/api/v1/users/register"))
                .hasValue(new OpenEndpointsCatalog.RateLimitRule(5, WINDOW));
    }

    @Test
    @DisplayName("property overrides replace the default limit and window")
    void propertyOverridesReplaceDefaults() {
        OpenEndpointsProperties properties = new OpenEndpointsProperties();
        OpenEndpointsProperties.LimitOverride override = new OpenEndpointsProperties.LimitOverride();
        override.setLimit(99);
        override.setWindow(Duration.ofMinutes(2));
        properties.getLimits().put("login", override);

        OpenEndpointsCatalog catalog = productionCatalog(properties);

        assertThat(catalog.findRule("POST", "/api/v1/auth/login"))
                .hasValue(new OpenEndpointsCatalog.RateLimitRule(99, Duration.ofMinutes(2)));
    }

    @Test
    @DisplayName("an override without a window keeps the default window")
    void overrideWithoutWindowKeepsDefaultWindow() {
        OpenEndpointsProperties properties = new OpenEndpointsProperties();
        OpenEndpointsProperties.LimitOverride override = new OpenEndpointsProperties.LimitOverride();
        override.setLimit(7);
        properties.getLimits().put("register", override);

        OpenEndpointsCatalog catalog = productionCatalog(properties);

        assertThat(catalog.findRule("POST", "/api/v1/users/register"))
                .hasValue(new OpenEndpointsCatalog.RateLimitRule(7, WINDOW));
    }

    @Test
    @DisplayName("unknown override names fail fast instead of silently doing nothing")
    void unknownOverrideNamesFailFast() {
        OpenEndpointsProperties properties = new OpenEndpointsProperties();
        OpenEndpointsProperties.LimitOverride override = new OpenEndpointsProperties.LimitOverride();
        override.setLimit(7);
        properties.getLimits().put("bogus-entry", override);

        assertThatThrownBy(() -> productionCatalog(properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bogus-entry")
                .hasMessageContaining("login");
    }

    @Test
    @DisplayName("a rule applies only to the endpoint's own HTTP method")
    void ruleAppliesOnlyToConfiguredMethod() {
        OpenEndpointsCatalog catalog = productionCatalog(new OpenEndpointsProperties());

        assertThat(catalog.findRule("GET", "/api/v1/auth/login")).isEmpty();
        assertThat(catalog.findRule("POST", "/api/v1/auth/login")).isPresent();
    }

    @Test
    @DisplayName("wildcard patterns match any HTTP method")
    void wildcardPatternsMatchAnyMethod() {
        OpenEndpointsCatalog catalog = productionCatalog(new OpenEndpointsProperties());

        assertThat(catalog.findRule("GET", "/v3/api-docs/school.yaml")).isPresent();
        assertThat(catalog.findRule("DELETE", "/v3/api-docs/school.yaml")).isPresent();
        assertThat(catalog.findRule("GET", "/swagger-ui/index.html")).isPresent();
    }

    @Test
    @DisplayName("paths outside the catalog have no rule")
    void pathsOutsideCatalogHaveNoRule() {
        OpenEndpointsCatalog catalog = productionCatalog(new OpenEndpointsProperties());

        assertThat(catalog.findRule("GET", "/api/v1/tasks")).isEmpty();
        assertThat(catalog.findRule("POST", "/api/v1/auth/login/extra")).isEmpty();
    }

    @Test
    @DisplayName("adding an entry to the catalog makes it public and rate-limited with no other edit")
    void addedEntryBecomesPublicAndLimited() {
        OpenEndpoint added = new OpenEndpoint(
                "test-hole", org.springframework.http.HttpMethod.POST,
                "/api/v1/test-hole", 5, WINDOW);
        List<OpenEndpoint> definitions = List.of(added);
        OpenEndpointsCatalog catalog = catalogFor(definitions, new OpenEndpointsProperties());

        assertThat(catalog.endpoints()).containsExactly(added);
        assertThat(catalog.findRule("POST", "/api/v1/test-hole"))
                .hasValue(new OpenEndpointsCatalog.RateLimitRule(5, WINDOW));
    }

    @Test
    @DisplayName("widestWindow reflects the largest configured window")
    void widestWindowReflectsLargestWindow() {
        OpenEndpointsProperties properties = new OpenEndpointsProperties();
        OpenEndpointsProperties.LimitOverride override = new OpenEndpointsProperties.LimitOverride();
        override.setWindow(Duration.ofMinutes(5));
        properties.getLimits().put("refresh", override);

        OpenEndpointsCatalog catalog = productionCatalog(properties);

        assertThat(catalog.widestWindow()).isEqualTo(Duration.ofMinutes(5));
    }
}
