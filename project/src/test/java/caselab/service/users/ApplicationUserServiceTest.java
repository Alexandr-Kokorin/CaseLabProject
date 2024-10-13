package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.exception.entity.UserNotFoundException;
import caselab.service.secutiry.AuthenticationService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationUserServiceTest {

    private final Long user1Id = 1L;
    @InjectMocks
    private ApplicationUserService userService;
    @Mock
    private ApplicationUserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationService authService;
    private ApplicationUser user1;
    private UserResponse userResponse1;
    private List<ApplicationUser> users;
    private List<UserResponse> userResponses;

    @BeforeEach
    void setUp() {
        Long user2Id = 2L;

        user1 = createUser(user1Id, "john_doe");
        ApplicationUser user2 = createUser(user2Id, "jane_doe");

        userResponse1 = createUserResponse(user1Id, "john_doe");
        UserResponse userResponse2 = createUserResponse(user2Id, "jane_doe");

        users = List.of(user1, user2);
        userResponses = List.of(userResponse1, userResponse2);
    }

    private ApplicationUser createUser(Long id, String email) {
        return ApplicationUser.builder()
            .id(id)
            .email(email)
            .displayName("John Doe")
            .hashedPassword("hashedpassword")
            .build();
    }

    private UserResponse createUserResponse(Long id, String email) {
        return UserResponse.builder()
            .id(id)
            .email(email)
            .displayName("John Doe")
            .build();
    }

    @Test
    void findAllUsers_shouldReturnListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(users);

        for (int i = 0; i < users.size(); i++) {
            when(userMapper.entityToResponse(users.get(i))).thenReturn(userResponses.get(i));
        }

        List<UserResponse> result = userService.findAllUsers();

        assertEquals(userResponses, result);
    }

    @Test
    void findUser_shouldReturnUserResponse() {
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        when(userMapper.entityToResponse(user1)).thenReturn(userResponse1);

        UserResponse result = userService.findUser(user1Id);

        assertEquals(userResponse1, result);
    }

    @Test
    void findUser_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(user1Id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUser(user1Id));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUserResponse() {
        Long user1Id = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest("john_updated", "NewPassword");
        UserResponse updatedUserResponse = UserResponse.builder().id(user1Id).email("john_updated").build();

        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        when(authService.encodePassword(updateRequest.password())).thenReturn("hashedPassword");
        when(userRepository.save(user1)).thenReturn(user1);
        when(userMapper.entityToResponse(user1)).thenReturn(updatedUserResponse);

        UserResponse result = userService.updateUser(user1Id, updateRequest);

        assertEquals(updatedUserResponse, result);
        verify(authService, times(1)).encodePassword(updateRequest.password());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenUserNotFound() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("john_updated", "NewPassword");

        when(userRepository.findById(user1Id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(user1Id, updateRequest));
    }

    @Test
    void deleteUser_shouldDeleteUser() {

        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));

        userService.deleteUser(user1Id);

        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(user1Id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(user1Id));

        verify(userRepository, times(0)).delete(any(ApplicationUser.class));
    }

    @Test
    void updateUser_shouldUpdatePasswordWhenNotEmpty() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(user1Id);
        existingUser.setEmail("john_doe");

        String newPassword = "NewPassword";
        UserUpdateRequest updateRequest = new UserUpdateRequest("john_updated", newPassword);

        UserResponse updatedUserResponse = UserResponse.builder().id(user1Id).email("john_updated").build();

        when(userRepository.findById(user1Id)).thenReturn(Optional.of(existingUser));
        when(authService.encodePassword(newPassword)).thenReturn("hashedPassword");
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.entityToResponse(existingUser)).thenReturn(updatedUserResponse);

        UserResponse result = userService.updateUser(user1Id, updateRequest);

        assertEquals(updatedUserResponse, result);
        verify(authService, times(1)).encodePassword(newPassword);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldNotUpdatePasswordWhenEmpty() {
        UserUpdateRequest updateRequestEmptyPassword = new UserUpdateRequest("john_updated", "");

        UserResponse updatedUserResponse = UserResponse.builder().id(user1Id).email("john_updated").build();

        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        when(userRepository.save(user1)).thenReturn(user1);
        when(userMapper.entityToResponse(user1)).thenReturn(updatedUserResponse);

        UserResponse resultEmptyPassword = userService.updateUser(user1Id, updateRequestEmptyPassword);

        assertEquals(updatedUserResponse, resultEmptyPassword);
        verify(authService, times(0)).encodePassword(any());
        verify(userRepository, times(1)).save(user1);
    }
}
