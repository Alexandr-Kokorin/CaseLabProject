package caselab.controller.security;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.service.secutiry.AuthenticationService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationControllerTest extends BaseControllerTest {

    private final String AUTH_URI = "/api/v1/auth";

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private SecurityFilterChain securityFilterChain;

    @Nested
    @Tag("Register")
    @DisplayName("Register user")
    class RegisterTest {

        @SneakyThrows
        @Test
        @DisplayName("Should register user with valid payload")
        public void register_success() {
            var payload = new RegisterRequest("test@mail.ru", "displayName", "password");
            var response = AuthenticationResponse.builder()
                .token("token")
                .build();

            when(authenticationService.register(payload)).thenReturn(response);

            var mvcResponse = mockMvc.perform(post(AUTH_URI + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpectAll(
                    status().is2xxSuccessful(),
                    content().contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse();

            var actualResponse = objectMapper.readValue(mvcResponse.getContentAsString(), AuthenticationResponse.class);

            assertEquals(actualResponse, response);
        }
    }

    @Nested
    @Tag("Authenticate")
    @DisplayName("Authenticate user")
    class AuthenticateTest {

        @SneakyThrows
        @Test
        @DisplayName("Should authenticate user with valid payload")
        public void authenticate_success() {
            var payload = AuthenticationRequest.builder()
                .email("test@mail.ru")
                .password("password")
                .build();
            var response = AuthenticationResponse.builder()
                .token("token")
                .build();

            when(authenticationService.authenticate(payload)).thenReturn(response);

            var mvcResponse = mockMvc.perform(post(AUTH_URI + "/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpectAll(
                    status().is2xxSuccessful(),
                    content().contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse();

            var actualResponse = objectMapper.readValue(mvcResponse.getContentAsString(), AuthenticationResponse.class);

            assertEquals(actualResponse, response);
        }
    }
}
