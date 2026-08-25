package com.school.school.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime tuning for the open endpoints catalog.
 *
 * <pre>
 * # optional per-entry overrides, keyed by catalog entry name
 * school.open-endpoints.limits.login.limit=15
 * school.open-endpoints.limits.register.window=PT2M
 *
 * # how many reverse proxies sit in front of the app (see ClientIpResolver)
 * school.open-endpoints.client-ip.trusted-proxies=0
 * </pre>
 */
@ConfigurationProperties(prefix = "school.open-endpoints")
public class OpenEndpointsProperties {

    private Map<String, LimitOverride> limits = new LinkedHashMap<>();
    private final ClientIp clientIp = new ClientIp();

    public Map<String, LimitOverride> getLimits() {
        return limits;
    }

    public void setLimits(Map<String, LimitOverride> limits) {
        this.limits = limits == null ? new LinkedHashMap<>() : limits;
    }

    public ClientIp getClientIp() {
        return clientIp;
    }

    public static class LimitOverride {

        private Integer limit;
        private Duration window;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }
    }

    public static class ClientIp {

        private int trustedProxies = 0;

        public int getTrustedProxies() {
            return trustedProxies;
        }

        public void setTrustedProxies(int trustedProxies) {
            this.trustedProxies = trustedProxies;
        }
    }
}
