package com.school.school.infra.security;

import com.school.school.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenEndpointRateLimitFilterTest {

    private static final String REGISTER_URI = "/api/v1/users/register";
    private static final int REGISTER_LIMIT = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final MutableClock clock = MutableClock.at("2026-01-01T10:00:00Z");
    private OpenEndpointRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new OpenEndpointRateLimitFilter(
                clock,
                new OpenEndpointsCatalog(OpenEndpoints.DEFINITIONS, new OpenEndpointsProperties()),
                resolverWith(0));
    }

    private static ClientIpResolver resolverWith(int trustedProxies) {
        OpenEndpointsProperties properties = new OpenEndpointsProperties();
        properties.getClientIp().setTrustedProxies(trustedProxies);
        return new ClientIpResolver(properties);
    }

    private MockHttpServletResponse post(String uri) throws Exception {
        return send("POST", uri);
    }

    private MockHttpServletResponse send(String method, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    @DisplayName("requests within the limit pass through to the chain")
    void requestsWithinLimitPassThrough() throws Exception {
        for (int i = 0; i < REGISTER_LIMIT; i++) {
            assertThat(post(REGISTER_URI).getStatus()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("the request over the limit is rejected with 429 and Retry-After")
    void requestOverLimitIsRejected() throws Exception {
        for (int i = 0; i < REGISTER_LIMIT; i++) {
            post(REGISTER_URI);
        }

        MockHttpServletResponse blocked = post(REGISTER_URI);

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo(String.valueOf(WINDOW.toSeconds()));
        assertThat(blocked.getContentAsString()).contains("Too many requests");
    }

    @Test
    @DisplayName("advancing the Clock past the window reopens the limit without real-time waiting")
    void advancingClockReopensWindow() throws Exception {
        for (int i = 0; i < REGISTER_LIMIT; i++) {
            post(REGISTER_URI);
        }
        assertThat(post(REGISTER_URI).getStatus()).isEqualTo(429);

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertThat(post(REGISTER_URI).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("endpoints outside the catalog are never limited")
    void uncataloguedEndpointsAreNeverLimited() throws Exception {
        for (int i = 0; i < REGISTER_LIMIT * 3; i++) {
            assertThat(send("GET", "/api/v1/tasks").getStatus()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("a spoofed forwarded header cannot open a second bucket for the same client")
    void spoofedForwardedHeaderDoesNotSplitBuckets() throws Exception {
        MockHttpServletRequest forged = new MockHttpServletRequest("POST", REGISTER_URI);
        forged.setRemoteAddr("10.0.0.1");
        forged.addHeader("X-Forwarded-For", "9.9.9.9");

        for (int i = 0; i < REGISTER_LIMIT; i++) {
            post(REGISTER_URI);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(forged, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("an entry added only to the catalog arrives already public-shaped and rate-limited")
    void catalogEntryAloneIsRateLimited() throws Exception {
        OpenEndpoint added = new OpenEndpoint(
                "test-hole", HttpMethod.POST, "/api/v1/test-hole", 2, WINDOW);
        OpenEndpointsCatalog catalog = new OpenEndpointsCatalog(List.of(added), new OpenEndpointsProperties());
        filter = new OpenEndpointRateLimitFilter(clock, catalog, resolverWith(0));

        assertThat(post("/api/v1/test-hole").getStatus()).isEqualTo(200);
        assertThat(post("/api/v1/test-hole").getStatus()).isEqualTo(200);
        assertThat(post("/api/v1/test-hole").getStatus()).isEqualTo(429);
    }
}
