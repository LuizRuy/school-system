package com.school.testsupport;

import com.school.school.infra.security.CustomAccessDeniedHandler;
import com.school.school.infra.security.CustomAuthenticationEntryPoint;
import com.school.school.infra.security.TokenAuthenticationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Security handler beans WebMvc slices need alongside {@code SecurityConfig}.
 */
@TestConfiguration
public class SecurityHandlersSliceConfig {

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(CustomAuthenticationEntryPoint entryPoint) {
        return new TokenAuthenticationFilter(null, null, entryPoint);
    }
}
