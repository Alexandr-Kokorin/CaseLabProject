package caselab.controller.users;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationUserControllerTest extends BaseControllerTest {

    private final String URL = "/api/v1/users";
    private static AuthenticationResponse authToken;

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
        var token = login().token();

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
        var token = login().token();
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
        var token = login().token();
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
        var token = login().token();
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
        var token = login().token();
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
                jsonPath("$.display_name").value(updateRequest.displayName()),
                jsonPath("$.document_ids").isEmpty()
            );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 400 for invalid user data in updateUser method")
    public void shouldReturn400ForInvalidUserDataInUpdateUser() {
        var token = login().token();
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
    public void shouldDeleteUser() {
        var token = login().token();

        mockMvc.perform(delete(URL)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 403 for unauthorized access to delete user")
    public void shouldReturn403ForUnauthorizedAccessToDeleteUser() {
        mockMvc.perform(delete(URL))
            .andExpect(status().isForbidden());
    }
}
