package caselab.service;

import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.service.users.ApplicationUserService;
import caselab.service.users.UserMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationUserServiceTest {
    @InjectMocks
    private ApplicationUserService userService;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Test
    void findAllUsers_shouldReturnListOfUserResponses() {
        List<ApplicationUser> users = List.of(
            ApplicationUser.builder().id(1L).login("john_doe").displayName("John Doe").hashedPassword("hashedpassword")
                .build(),
            ApplicationUser.builder().id(2L).login("jane_doe").displayName("John Doe").hashedPassword("hashedpassword")
                .build()
        );

        List<UserResponse> userResponses = List.of(
            UserResponse.builder().id(1L).login("john_doe").displayName("John Doe")
                .build(),
            UserResponse.builder().id(2L).login("john_doe").displayName("John Doe")
                .build()
        );

        when(userRepository.findAll()).thenReturn(users);

        for (int i = 0; i < users.size(); i++) {
            when(userMapper.entityToResponse(users.get(i))).thenReturn(userResponses.get(i));
        }

        List<UserResponse> result = userService.findAllUsers();

        assertEquals(userResponses, result);
    }
}
