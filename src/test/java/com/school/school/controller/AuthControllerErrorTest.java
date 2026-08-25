package com.school.school.controller;

import com.school.school.infra.exception.GlobalExceptionHandler;
import com.school.school.infra.security.ExpiredRefreshTokenException;
import com.school.school.infra.security.InvalidRefreshTokenException;
import com.school.school.infra.security.SecurityConfig;
import com.school.school.service.AuthService;
import com.school.testsupport.OpenEndpointsSliceConfig;
import com.school.testsupport.SecurityHandlersSliceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, OpenEndpointsSliceConfig.class,
        SecurityHandlersSliceConfig.class})
class AuthControllerErrorTest {

    private static final String REFRESH_URI = "/api/v1/auth/refresh";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("an unknown refresh token returns 401 with an invalid-token reason")
    void unknownRefreshTokenReturnsUnauthorizedInvalid() throws Exception {
        Mockito.when(authService.refreshToken("never-issued-refresh-token"))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post(REFRESH_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"never-issued-refresh-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token is invalid"));
    }

    @Test
    @DisplayName("an expired refresh token returns 401 with a distinct expired-token reason")
    void expiredRefreshTokenReturnsUnauthorizedExpired() throws Exception {
        Mockito.when(authService.refreshToken("stale-expired-refresh-token"))
                .thenThrow(new ExpiredRefreshTokenException());

        mockMvc.perform(post(REFRESH_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"stale-expired-refresh-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token has expired. Please make a new signin request"));
    }
}
