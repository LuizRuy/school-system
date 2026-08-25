package com.school.school.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private ClientIpResolver resolverWith(int trustedProxies) {
        OpenEndpointsProperties properties = new OpenEndpointsProperties();
        properties.getClientIp().setTrustedProxies(trustedProxies);
        return new ClientIpResolver(properties);
    }

    private MockHttpServletRequest requestFrom(String remoteAddr, String xForwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        if (xForwardedFor != null) {
            request.addHeader("X-Forwarded-For", xForwardedFor);
            request.addHeader("X-Real-IP", "6.6.6.6");
        }
        return request;
    }

    @Test
    @DisplayName("by default only the TCP peer address counts; forwarded headers are ignored")
    void forwardedHeadersIgnoredByDefault() {
        ClientIpResolver resolver = resolverWith(0);

        String ip = resolver.resolve(requestFrom("203.0.113.7", "9.9.9.9"));

        assertThat(ip).isEqualTo("203.0.113.7");
    }

    @Test
    @DisplayName("with two trusted proxies, the hop appended by the outermost trusted proxy wins")
    void trustedProxyCountPicksHopAppendedByOutermostTrustedProxy() {
        ClientIpResolver resolver = resolverWith(2);

        String ip = resolver.resolve(requestFrom("10.0.0.2", "9.9.9.9, 1.2.3.4, 10.0.0.1"));

        assertThat(ip).isEqualTo("1.2.3.4");
    }

    @Test
    @DisplayName("with one trusted proxy, only the hop that proxy appended counts; a forged prefix is ignored")
    void forgedPrefixBeyondTrustedHopCountIsIgnored() {
        ClientIpResolver resolver = resolverWith(1);

        String ip = resolver.resolve(requestFrom("10.0.0.2", "9.9.9.9, 1.2.3.4"));

        assertThat(ip).isEqualTo("1.2.3.4");
    }

    @Test
    @DisplayName("a missing header falls back to the TCP peer address")
    void missingHeaderFallsBackToRemoteAddr() {
        ClientIpResolver resolver = resolverWith(2);

        String ip = resolver.resolve(requestFrom("10.0.0.2", null));

        assertThat(ip).isEqualTo("10.0.0.2");
    }

    @Test
    @DisplayName("fewer hops than configured trusted proxies falls back to the TCP peer address")
    void fewerHopsThanTrustedProxiesFallsBack() {
        ClientIpResolver resolver = resolverWith(3);

        String ip = resolver.resolve(requestFrom("10.0.0.2", "1.2.3.4"));

        assertThat(ip).isEqualTo("10.0.0.2");
    }
}
