package caselab.controller.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.service.secutiry.JwtService;
import caselab.service.users.ApplicationUserService;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationUserController.class)
class ApplicationUserControllerTest {

    private final String USERS_URI = "/api/v1/users";
    private MockMvc mockMvc;

    @MockBean
    private ApplicationUserService userService;
    @MockBean
    private JwtService jwtService;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        userResponse = UserResponse.builder()
            .email("john_doe")
            .displayName("John Doe")
            .documentIds(List.of())
            .build();
    }
/*
    @Test
    @WithMockUser
    void findAllUsers_shouldReturnUserList() throws Exception {
        List<UserResponse> users = Collections.singletonList(userResponse);
        when(userService.findAllUsers()).thenReturn(users);

        mockMvc.perform(get(USERS_URI))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value(userResponse.email()))
            .andExpect(jsonPath("$[0].displayName").value(userResponse.displayName()))
            .andExpect(jsonPath("$[0].documents").isArray());
    }

    @Test
    @WithMockUser
    void findUserById_shouldReturnUser() throws Exception {
        when(userService.findUser(userResponse.email())).thenReturn(userResponse);

        mockMvc.perform(get(USERS_URI + "/" + 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(userResponse.email()))
            .andExpect(jsonPath("$.documents").isArray());
    }

    @Test
    @WithMockUser(username = "john_doe", authorities = "USER")
    void updateUser_shouldUpdateUser_WhenUserIsOwner() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
            .displayName("John Updated")
            .password("new_password")
            .build();

        when(userService.updateUser(any(Authentication.class), updateRequest)).thenReturn(userResponse);

        mockMvc.perform(put(USERS_URI + "/" + 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john_doe", authorities = "USER")
    void deleteUser_shouldDeleteUser_WhenUserIsOwner() throws Exception {
        mockMvc.perform(delete(USERS_URI + "/" + 1L))
            .andExpect(status().isNoContent());
    }
    */
}
