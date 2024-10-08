package caselab.controller.user;

import caselab.controller.users.ApplicationUserController;
import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.service.secutiry.JwtService;
import caselab.service.users.ApplicationUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationUserController.class)
class ApplicationUserControllerTest {
    private MockMvc mockMvc;

    @MockBean
    private ApplicationUserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        userResponse = UserResponse.builder()
            .id(1L)
            .login("john_doe")
            .displayName("John Doe")
            .documents(List.of())
            .build();
    }

    @Test
    @WithMockUser
    void findAllUsers_shouldReturnUserList() throws Exception {
        List<UserResponse> users = Collections.singletonList(userResponse);
        when(userService.findAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(userResponse.id()))
            .andExpect(jsonPath("$[0].login").value(userResponse.login()))
            .andExpect(jsonPath("$[0].displayName").value(userResponse.displayName()))
            .andExpect(jsonPath("$[0].documents").isArray());
    }

    @Test
    @WithMockUser
    void findUserById_shouldReturnUser() throws Exception {
        when(userService.findUser(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userResponse.id()))
            .andExpect(jsonPath("$.login").value(userResponse.login()))
            .andExpect(jsonPath("$.documents").isArray());
    }

    @Test
    @WithMockUser(username = "john_doe", authorities = "USER")
    void updateUser_shouldUpdateUser_WhenUserIsOwner() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
            .displayName("John Updated")
            .password("new_password")
            .build();

        when(userService.updateUser(1L, updateRequest)).thenReturn(userResponse);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john_doe", authorities = "USER")
    void deleteUser_shouldDeleteUser_WhenUserIsOwner() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
            .andExpect(status().isNoContent());
    }
}
