package com.school.testsupport;

import com.school.school.infra.security.ClientIpResolver;
import com.school.school.infra.security.OpenEndpointRateLimitFilter;
import com.school.school.infra.security.OpenEndpointsCatalog;
import com.school.school.infra.security.OpenEndpointsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Real open-endpoint beans (default catalog, default properties) for WebMvc
 * slices that import {@code SecurityConfig}.
 */
@Configuration
public class OpenEndpointsSliceConfig {

    @Bean
    public OpenEndpointsProperties openEndpointsProperties() {
        return new OpenEndpointsProperties();
    }

    @Bean
    public OpenEndpointsCatalog openEndpointsCatalog(OpenEndpointsProperties properties) {
        return new OpenEndpointsCatalog(properties);
    }

    @Bean
    public ClientIpResolver clientIpResolver(OpenEndpointsProperties properties) {
        return new ClientIpResolver(properties);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public OpenEndpointRateLimitFilter openEndpointRateLimitFilter(Clock clock,
                                                                   OpenEndpointsCatalog catalog,
                                                                   ClientIpResolver clientIpResolver) {
        return new OpenEndpointRateLimitFilter(clock, catalog, clientIpResolver);
    }
}
