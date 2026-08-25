package com.school.school.infra.security;

import com.school.school.support.MutableClock;
import com.school.testsupport.SecurityHandlersSliceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves ticket 08's core promise: an endpoint that appears only as a catalog
 * entry — with no other edit — is served anonymously and rate-limited through
 * the real {@link SecurityConfig}.
 */
@WebMvcTest(OpenEndpointCatalogWiringTest.TestHoleController.class)
@Import({SecurityConfig.class, OpenEndpointCatalogWiringTest.CustomCatalogSlice.class,
        SecurityHandlersSliceConfig.class})
class OpenEndpointCatalogWiringTest {

    private static final String HOLE_URI = "/api/v1/test-hole";
    private static final int HOLE_LIMIT = 3;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("an entry added only to the catalog is public and rate-limited, no other edit")
    void catalogEntryAloneIsPublicAndRateLimited() throws Exception {
        mockMvc.perform(post(HOLE_URI)).andExpect(status().isOk());

        for (int i = 0; i < HOLE_LIMIT - 1; i++) {
            mockMvc.perform(post(HOLE_URI)).andExpect(status().isOk());
        }

        mockMvc.perform(post(HOLE_URI)).andExpect(status().is(429));
    }

    @Test
    @DisplayName("paths outside the catalog stay authenticated")
    void pathsOutsideCatalogStayProtected() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")).andExpect(status().isUnauthorized());
    }

    @RestController
    static class TestHoleController {

        @PostMapping(HOLE_URI)
        public ResponseEntity<String> hole() {
            return ResponseEntity.ok("through");
        }
    }

    /**
     * The open-endpoint beans, rebuilt around a catalog holding one extra
     * entry — the only edit a new public endpoint should ever need.
     */
    @TestConfiguration
    static class CustomCatalogSlice {

        @Bean
        OpenEndpointsProperties openEndpointsProperties() {
            return new OpenEndpointsProperties();
        }

        @Bean
        OpenEndpointsCatalog openEndpointsCatalog(OpenEndpointsProperties properties) {
            List<OpenEndpoint> definitions = new ArrayList<>(OpenEndpoints.DEFINITIONS);
            definitions.add(new OpenEndpoint(
                    "test-hole", org.springframework.http.HttpMethod.POST,
                    HOLE_URI, HOLE_LIMIT, java.time.Duration.ofMinutes(1)));
            return new OpenEndpointsCatalog(definitions, properties);
        }

        @Bean
        ClientIpResolver clientIpResolver(OpenEndpointsProperties properties) {
            return new ClientIpResolver(properties);
        }

        @Bean
        MutableClock mutableClock() {
            return MutableClock.at("2026-01-01T10:00:00Z");
        }

        @Bean
        OpenEndpointRateLimitFilter openEndpointRateLimitFilter(MutableClock clock,
                                                                OpenEndpointsCatalog catalog,
                                                                ClientIpResolver clientIpResolver) {
            return new OpenEndpointRateLimitFilter(clock, catalog, clientIpResolver);
        }

        @Bean
        TestHoleController testHoleController() {
            return new TestHoleController();
        }
    }
}
