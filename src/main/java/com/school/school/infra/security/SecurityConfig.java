package com.school.school.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/users/register").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()

                        .requestMatchers("/api/v1/users/update").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/users/disable/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/change-password").hasAnyRole("ADMIN", "USER")

                        .requestMatchers("/api/v1/students", "/api/v1/students/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/attendances", "/api/v1/attendances/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/classrooms", "/api/v1/classrooms/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/class-sessions", "/api/v1/class-sessions/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/submissions", "/api/v1/submissions/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/tasks", "/api/v1/tasks/**").hasAnyRole("ADMIN", "USER")

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
