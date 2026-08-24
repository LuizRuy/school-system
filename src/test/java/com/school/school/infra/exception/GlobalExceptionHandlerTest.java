package com.school.school.infra.exception;

import com.school.school.controller.UserController;
import com.school.school.infra.security.CustomAccessDeniedHandler;
import com.school.school.infra.security.CustomAuthenticationEntryPoint;
import com.school.school.infra.security.OpenEndpointRateLimitFilter;
import com.school.school.infra.security.SecurityConfig;
import com.school.school.infra.security.TokenAuthenticationFilter;
import com.school.school.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandlerTest.SliceSecurityBeans.class})
class GlobalExceptionHandlerTest {

    private static final String REGISTER_URI = "/api/v1/users/register";
    private static final String SECRET_DETAIL = "jdbc:postgresql://internal-db:5432/production";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @TestConfiguration
    static class SliceSecurityBeans {

        @Bean
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
            return new CustomAuthenticationEntryPoint();
        }

        @Bean
        CustomAccessDeniedHandler customAccessDeniedHandler() {
            return new CustomAccessDeniedHandler();
        }

        @Bean
        OpenEndpointRateLimitFilter openEndpointRateLimitFilter() {
            return new OpenEndpointRateLimitFilter();
        }

        @Bean
        TokenAuthenticationFilter tokenAuthenticationFilter(CustomAuthenticationEntryPoint entryPoint) {
            return new TokenAuthenticationFilter(null, null, entryPoint);
        }
    }

    private String validRegisterBody() {
        return """
                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"password123"}
                """;
    }

    @Test
    @DisplayName("@Valid failure returns 400 with field errors")
    void validationFailureReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Lovelace","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").value("Nome é obrigatorio"))
                .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("Malformed JSON returns 400, not 500")
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    @DisplayName("Foreign-key violation returns 409, not 500")
    void integrityViolationReturns409() throws Exception {
        Mockito.doThrow(new DataIntegrityViolationException("fk_users_tasks detail"))
                .when(userService).disable(7L);

        mockMvc.perform(patch("/api/v1/users/disable/7")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Data integrity violation"));
    }

    @Test
    @DisplayName("Malformed request parameters return 400")
    void malformedRequestParametersReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/users/not-a-number")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request parameters"));
    }

    @Test
    @DisplayName("Unexpected exception returns generic 500 without leaking internals")
    void unexpectedExceptionReturnsGeneric500WithoutInternals() throws Exception {
        Mockito.when(userService.getUserById(7L))
                .thenThrow(new RuntimeException(SECRET_DETAIL));

        MvcResult result = mockMvc.perform(get("/api/v1/users/7")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain(SECRET_DETAIL);
    }

    @Test
    @DisplayName("Entity not found keeps 404")
    void entityNotFoundKeeps404() throws Exception {
        Mockito.when(userService.getUserById(99L))
                .thenThrow(new EntityNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/v1/users/99")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    @DisplayName("Entity already exists keeps 409")
    void entityAlreadyExistsKeeps409() throws Exception {
        Mockito.doThrow(new EntityAlreadyExistsException("Email already registered"))
                .when(userService).save(Mockito.any());

        mockMvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    @DisplayName("Business rule violation keeps 400")
    void businessRuleKeeps400() throws Exception {
        Mockito.doThrow(new BusinessException("Password does not match the required policy"))
                .when(userService).save(Mockito.any());

        mockMvc.perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Password does not match the required policy"));
    }

    @Test
    @DisplayName("Access denied keeps 403")
    void accessDeniedKeeps403() throws Exception {
        Mockito.when(userService.getUserById(7L))
                .thenThrow(new AccessDeniedException("You do not have permission"));

        mockMvc.perform(get("/api/v1/users/7")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
