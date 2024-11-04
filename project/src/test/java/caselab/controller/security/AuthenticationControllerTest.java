package caselab.controller.security;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
import caselab.service.secutiry.ClaimsExtractorService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationControllerTest extends BaseControllerTest {

    private static final String AUTH_URI = "/api/v1/auth";

    @Autowired
    private ClaimsExtractorService claimsExtractorService;
    @MockBean
    private EmailService emailService;
    private String token;
    private String adminToken;
    private String email;

    @AfterEach
    public void cleanUp() {
        deleteTestUser();
    }

    @BeforeEach
    public void setUp() {
        doNothing().when(emailService).sendNotification(any(EmailNotificationDetails.class));
        adminToken = login("admin@gmail.com", "admin321@&123");
    }


    @SneakyThrows
    @Test
    @DisplayName("Регистрация пользователя с корректными данными")
    public void shouldRegisterUserSuccessfully() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@mail.ru")
            .displayName("displayName")
            .password("password")
            .build();
        email = registerRequest.email();
        // Выполняем регистрацию и проверяем токен
        token = performRegistrationAndGetToken(registerRequest,adminToken);

        // Проверяем содержимое токена
        assertTokenContainsEmailAndIsValid(token, registerRequest.email());
    }

    @Test
    @DisplayName("Ошибка при повторной регистрации")
    void shouldFailIfUserAlreadyExists() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test2@mail.ru")
            .displayName("displayName")
            .password("password")
            .build();
        email = registerRequest.email();
        // Первичная регистрация
        token = performRegistrationAndGetToken(registerRequest,adminToken);

        // Повторная регистрация
        mockMvc.perform(post(AUTH_URI + "/register")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpectAll(
                status().isConflict()
            );
    }

    //Использовал пользователя ADMIN - так как присутсвует регистрация
    @Test
    @DisplayName("Успешная аутентификация пользователя")
    void shouldAuthenticateUserSuccessfully() throws Exception {

        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test3@mail.ru")
            .displayName("displayName")
            .password("password")
            .build();
        email = registerRequest.email();
        // Первичная регистрация
        performRegistrationAndGetToken(registerRequest,adminToken);

        // Аутентификация
        var authRequest = AuthenticationRequest.builder()
            .email(registerRequest.email())
            .password(registerRequest.password())
            .build();
        token = performAuthenticationAndGetToken(authRequest);
        // Проверяем содержимое токена
        assertTokenContainsEmailAndIsValid(token, registerRequest.email());

    }

    @Test
    @DisplayName("Ошибка аутентификации - неверный пароль")
    void shouldFailAuthenticationWithInvalidPassword() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test4@mail.ru")
            .displayName("displayName")
            .password("password")
            .build();
        email = registerRequest.email();
        // Первичная регистрация
        token = performRegistrationAndGetToken(registerRequest,adminToken);

        // Аутентификация с неверным паролем
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
            .email(
                registerRequest.email())
            .password("wrongPassword")
            .build();

        mockMvc.perform(post(AUTH_URI + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isUnauthorized());
    }
    @SneakyThrows
    private String login(String email, String password) {
        var request = AuthenticationRequest.builder()
            .email(email)
            .password(password)
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        return readValue(mvcResponse, AuthenticationResponse.class).token();
    }
    @SneakyThrows
    private String performRegistrationAndGetToken(RegisterRequest request,String token) {
        var response = mockMvc.perform(post(AUTH_URI + "/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpectAll(
                status().is2xxSuccessful(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.token", notNullValue())
            )
            .andReturn();

        var authenticationResponse = readValue(
            response,
            AuthenticationResponse.class
        );
        return authenticationResponse.token();
    }

    @SneakyThrows
    private String performAuthenticationAndGetToken(AuthenticationRequest request) {
        var response = mockMvc.perform(post(AUTH_URI + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.token", notNullValue())
            )
            .andReturn();

        var authenticationResponse = readValue(
            response,
            AuthenticationResponse.class
        );
        return authenticationResponse.token();
    }

    private void assertTokenContainsEmailAndIsValid(String token, String expectedEmail) {
        assertAll(
            () -> assertNotNull(token, "Токен не должен быть null"),
            () -> assertEquals(expectedEmail, claimsExtractorService.extractEmail(token),
                "Email внутри токена должен совпадать с email пользователя"
            ),
            () -> assertFalse(claimsExtractorService.isTokenExpired(token), "Токен не должен быть просрочен")
        );
    }

    @SneakyThrows
    private <T> T readValue(MvcResult mvcResponse, Class<T> valueType) {
        return objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            valueType
        );
    }

    @SneakyThrows
    private void deleteTestUser() {
        mockMvc.perform(delete("/api/v1/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("email", email))
            .andExpect(status().isNoContent());
    }
}
