package com.school.school.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "school.open-endpoints.limits.login.limit=99",
        "school.open-endpoints.limits.login.window=PT2M",
        "school.open-endpoints.client-ip.trusted-proxies=2"
})
class OpenEndpointsPropertiesBindingTest {

    @Autowired
    private OpenEndpointsCatalog catalog;

    @Autowired
    private ClientIpResolver clientIpResolver;

    @Test
    @DisplayName("limit and window overrides bind through Spring and reach the catalog's rules")
    void limitAndWindowOverridesBind() {
        assertThat(catalog.findRule("POST", "/api/v1/auth/login"))
                .contains(new OpenEndpointsCatalog.RateLimitRule(99, Duration.ofMinutes(2)));
    }

    @Test
    @DisplayName("trusted-proxies binds through Spring and drives client IP resolution")
    void trustedProxiesBinds() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Forwarded-For", "9.9.9.9, 1.2.3.4, 10.0.0.1");

        assertThat(clientIpResolver.resolve(request)).isEqualTo("1.2.3.4");
    }
}
