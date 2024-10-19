package caselab.controller.users;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
            .email("user@example.com")
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

    @Nested
    @DisplayName("Tests for User Controller")
    class UserControllerTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return list of all users")
        public void shouldReturnAllUsers() {
            var token = login().token();

            mockMvc.perform(get(URL + "/all")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document_ids").isEmpty());
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
    }
}
