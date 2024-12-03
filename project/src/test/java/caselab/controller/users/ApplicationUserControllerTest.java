package caselab.controller.users;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.search.SearchRequest;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationUserControllerTest extends BaseControllerTest {

    private static AuthenticationResponse authToken;
    private final String AUTH_URI = "/api/v1/auth";
    private final String URL = "/api/v1/users";
    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        doNothing().when(emailService).sendNotification(any(EmailNotificationDetails.class));
    }

    @SneakyThrows
    private AuthenticationResponse login() {
        if (authToken != null) {
            return authToken;
        }

        var request = AuthenticationRequest.builder()
            .email("auth@example.com")
            .password("password")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        authToken = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return authToken;
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return list of all users")
    public void shouldReturnAllUsers() {
        var token = login().accessToken();

        mockMvc.perform(get(URL + "/all")
                .header("Authorization", "Bearer " + token))
            .andExpectAll(
                status().isOk(),
                jsonPath("$").isArray()
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 403 for unauthorized access to get all users")
    public void shouldReturn403ForUnauthorizedAccessToGetAllUsers() {
        mockMvc.perform(get(URL + "/all"))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should find user by email")
    public void shouldFindUserByEmail() {
        var token = login().accessToken();
        String email = "auth@example.com";

        mockMvc.perform(get(URL)
                .header("Authorization", "Bearer " + token)
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.email").value(email),
                jsonPath("$.display_name").isNotEmpty()
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 400 for invalid email")
    public void shouldReturn400ForInvalidEmail() {
        var token = login().accessToken();
        String invalidEmail = "";

        mockMvc.perform(get(URL)
                .header("Authorization", "Bearer " + token)
                .param("email", invalidEmail)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 403 for unauthorized access to find user by email")
    public void shouldReturn403ForUnauthorizedAccessToFindUserByEmail() {
        String email = "auth@example.com";
        mockMvc.perform(get(URL)
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 for non-existing user email")
    public void shouldReturn404ForNonExistingUserEmail() {
        var token = login().accessToken();
        String nonExistingEmail = "nonexistent@example.com";

        mockMvc.perform(get(URL)
                .header("Authorization", "Bearer " + token)
                .param("email", nonExistingEmail)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should update user information successfully")
    public void shouldUpdateUser() {
        var token = login().accessToken();
        var updateRequest = UserUpdateRequest.builder()
            .displayName("Updated name")
            .password("Updated password")
            .build();

        var request = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put(URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.display_name").value(updateRequest.displayName())
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 400 for invalid user data in updateUser method")
    public void shouldReturn400ForInvalidUserDataInUpdateUser() {
        var token = login().accessToken();
        var invalidUpdateRequest = UserUpdateRequest.builder()
            .displayName("")
            .password("short")
            .build();

        mockMvc.perform(put(URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 403 for unauthorized access to update user")
    public void shouldReturn403ForUnauthorizedAccessToUpdateUser() {
        var updateRequest = UserUpdateRequest.builder()
            .displayName("Updated name")
            .password("Updated password")
            .build();

        var request = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should delete user successfully")
    @WithMockUser(username = "admin@gmail.com", roles = "{ADMIN}")
    public void shouldDeleteUser() {
        var token = login().accessToken();

        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test123@mail.ru")
            .displayName("displayName")
            .password("password")
            .build();
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 403 for unauthorized access to delete user")
    public void shouldReturn403ForUnauthorizedAccessToDeleteUser() {
        mockMvc.perform(delete(URL))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return filtered list of users")
    public void shouldReturnFilteredUsers() {
        var token = login().accessToken();

        var searchRequest = SearchRequest.builder()
            .filters(Map.of(
                "email", List.of("user@example.com")
            ))
            .build();

        mockMvc.perform(post(URL + "/all/advanced_search")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$").isArray(),
                jsonPath("$[0].email").value("user@example.com"),
                jsonPath("$[0].roles").isArray()
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return empty list for unmatched filters")
    public void shouldReturnEmptyListForUnmatchedFilters() {
        var token = login().accessToken();

        var searchRequest = SearchRequest.builder()
            .filters(Map.of(
                "email", List.of("nonexistent@example.com")
            ))
            .build();

        mockMvc.perform(post(URL + "/all/advanced_search")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$").isArray(),
                jsonPath("$").isEmpty()
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 400 for invalid search request")
    public void shouldReturn400ForInvalidSearchRequest() {
        var token = login().accessToken();

        mockMvc.perform(post(URL + "/all/advanced_search")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 403 for unauthorized access to advanced search")
    public void shouldReturn403ForUnauthorizedAccessToAdvancedSearch() {
        var searchRequest = SearchRequest.builder()
            .filters(Map.of(
                "email", List.of("user@example.com")
            ))
            .build();

        mockMvc.perform(post(URL + "/all/advanced_search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
            .andExpect(status().isForbidden());
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
            .andReturn();

        var authenticationResponse = readValue(
            response,
            AuthenticationResponse.class
        );
        return authenticationResponse.accessToken();
    }

    @SneakyThrows
    private <T> T readValue(MvcResult mvcResponse, Class<T> valueType) {
        return objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            valueType
        );
    }
}
