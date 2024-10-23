package caselab.controller.security;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.service.secutiry.ClaimsExtractorService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationControllerTest extends BaseControllerTest {

    private static final String AUTH_URI = "/api/v1/auth";

    @Autowired
    private ClaimsExtractorService claimsExtractorService;

    @SneakyThrows
    @Test
    @DisplayName("Регистрация пользователя с корректными данными")
    public void shouldRegisterUserSuccessfully() {
        RegisterRequest registerRequest = createRegisterRequest("test@mail.ru",
            "displayName",
            "password");

        // Выполняем регистрацию и проверяем токен
        String token = performRegistrationAndGetToken(registerRequest);

        // Проверяем содержимое токена
        assertTokenContainsEmailAndIsValid(token, registerRequest.email());
    }

    @Test
    @DisplayName("Ошибка при повторной регистрации")
    void shouldFailIfUserAlreadyExists() throws Exception {
        RegisterRequest registerRequest = createRegisterRequest("testRepeat@mail.ru",
            "displayName",
            "password");

        // Первичная регистрация
        performRegistrationAndGetToken(registerRequest);

        // Повторная регистрация
        mockMvc.perform(post(AUTH_URI + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Успешная аутентификация пользователя")
    void shouldAuthenticateUserSuccessfully() throws Exception {
        RegisterRequest registerRequest = createRegisterRequest("testAuth@mail.ru",
            "displayName",
            "password");

        // Первичная регистрация
        performRegistrationAndGetToken(registerRequest);

        // Аутентификация
        AuthenticationRequest authRequest = createAuthenticationRequest(registerRequest.email(), registerRequest.password());
        String token = performAuthenticationAndGetToken(authRequest);

        // Проверяем содержимое токена
        assertTokenContainsEmailAndIsValid(token, registerRequest.email());
    }

    @Test
    @DisplayName("Ошибка аутентификации - неверный пароль")
    void shouldFailAuthenticationWithInvalidPassword() throws Exception {
        RegisterRequest registerRequest = createRegisterRequest("testAuthError@mail.ru",
            "displayName",
            "password");

        // Первичная регистрация
        performRegistrationAndGetToken(registerRequest);

        // Аутентификация с неверным паролем
        AuthenticationRequest authRequest = createAuthenticationRequest(registerRequest.email(),
            "wrongPassword");

        mockMvc.perform(post(AUTH_URI + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isUnauthorized());
    }

    // Вспомогательные методы
    private RegisterRequest createRegisterRequest(String email, String displayName, String password) {
        return RegisterRequest.builder()
            .email(email)
            .displayName(displayName)
            .password(password)
            .build();
    }

    private AuthenticationRequest createAuthenticationRequest(String email, String password) {
        return AuthenticationRequest.builder()
            .email(email)
            .password(password)
            .build();
    }

    @SneakyThrows
    private String performRegistrationAndGetToken(RegisterRequest request) {
        var response = mockMvc.perform(post(AUTH_URI + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpectAll(
                status().is2xxSuccessful(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.token", notNullValue())
            )
            .andReturn()
            .getResponse();

        var authenticationResponse = objectMapper.readValue(response.getContentAsString(),
            AuthenticationResponse.class);
        return authenticationResponse.token();
    }

    @SneakyThrows
    private String performAuthenticationAndGetToken(AuthenticationRequest request) {
        var response = mockMvc.perform(post(AUTH_URI + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()))
            .andReturn()
            .getResponse();

        var authenticationResponse = objectMapper.readValue(response.getContentAsString(),
            AuthenticationResponse.class);
        return authenticationResponse.token();
    }

    private void assertTokenContainsEmailAndIsValid(String token, String expectedEmail) {
        assertAll(
            () -> assertNotNull(token, "Токен не должен быть null"),
            () -> assertEquals(expectedEmail, claimsExtractorService.extractEmail(token),
                "Email внутри токена должен совпадать с email пользователя"),
            () -> assertFalse(claimsExtractorService.isTokenExpired(token), "Токен не должен быть просрочен")
        );
    }
}
