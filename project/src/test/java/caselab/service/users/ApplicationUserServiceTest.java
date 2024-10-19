package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.service.secutiry.AuthenticationService;
import caselab.service.users.mapper.UserMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationUserServiceTest {

    private final Long user1Id = 1L;
    private final String userEmail = "johnDoe@gmail.com";
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

        user1 = createUser(user1Id, "johnDoe@gmail.com", "john_doe", "PBKDF2WithHmacSHA512");
        ApplicationUser user2 = createUser(user2Id, "janeDoe@gmail.com", "jane_doe", "BthHmacSHA512");

        userResponse1 = createUserResponse("johnDoe@gmail.com", "john_doe", new ArrayList<>());
        UserResponse userResponse2 = createUserResponse("jane_doe", "janeDoe@gmail.com", new ArrayList<>());

        users = List.of(user1, user2);
        userResponses = List.of(userResponse1, userResponse2);
    }

    private ApplicationUser createUser(Long id, String email, String displayName, String hashedPassword) {
        return ApplicationUser.builder()
            .id(id)
            .email(email)
            .displayName(displayName)
            .hashedPassword(hashedPassword)
            .build();
    }

    private UserResponse createUserResponse(String email, String displayName, List<Long> documentsIds) {
        return UserResponse.builder()
            .email(email)
            .displayName(displayName)
            .documentIds(documentsIds)
            .build();
    }

    private UserUpdateRequest createUserUpdateRequest(String displayName, String password) {
        return UserUpdateRequest.builder()
            .displayName(displayName)
            .password(password)
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
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user1));
        when(userMapper.entityToResponse(user1)).thenReturn(userResponse1);

        UserResponse result = userService.findUser(user1.getEmail());

        assertEquals(userResponse1, result);
    }

    @Test
    void findUser_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findUser(user1.getEmail()));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUserResponse() {
        UserUpdateRequest updateRequest = createUserUpdateRequest("JohnNewName", "newPassword");
        UserResponse updatedUserResponse = createUserResponse(userEmail, "JohnNewName", new ArrayList<>());

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user1));
        when(authService.encodePassword(updateRequest.password())).thenReturn("hashedPassword");
        when(userRepository.save(user1)).thenReturn(user1);
        when(userMapper.entityToResponse(user1)).thenReturn(updatedUserResponse);

        UserResponse result = userService.updateUser(authentication, updateRequest);

        assertEquals(updatedUserResponse, result);
        verify(authService, times(1)).encodePassword(updateRequest.password());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenUserNotFound() {
        UserUpdateRequest updateRequest = createUserUpdateRequest("john_updated", "NewPassword");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.updateUser(authentication, updateRequest));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user1));

        userService.deleteUser(authentication);

        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.deleteUser(authentication));

        verify(userRepository, times(0)).delete(any(ApplicationUser.class));
    }

    @Test
    void updateUser_shouldUpdatePasswordWhenNotEmpty() {
        String newPassword = "NewPassword";
        UserUpdateRequest updateRequest = createUserUpdateRequest("john_updated", newPassword);
        ApplicationUser existingUser = createUser(user1Id, "johnDoe@gmail.com", "john_doe", "oldPassword");
        UserResponse updatedUserResponse = createUserResponse("johnDoe@gmail.com", "john_updated", new ArrayList<>());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(existingUser);

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));
        when(authService.encodePassword(newPassword)).thenReturn("hashedPassword");
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.entityToResponse(existingUser)).thenReturn(updatedUserResponse);

        UserResponse result = userService.updateUser(authentication, updateRequest);

        assertEquals(updatedUserResponse, result);
        verify(authService, times(1)).encodePassword(newPassword);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldUpdatePassword() {
        String newPassword = "NewPassword";
        UserUpdateRequest updateRequest = createUserUpdateRequest("JohnNewName", newPassword);

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);

        ApplicationUser existingUser = createUser(user1Id, "johnDoe@gmail.com", "john_doe", "oldPassword");
        UserResponse updatedUserResponse = createUserResponse("johnDoe@gmail.com", "john_updated", new ArrayList<>());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(existingUser));
        when(authService.encodePassword(newPassword)).thenReturn("hashedPassword");
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.entityToResponse(existingUser)).thenReturn(updatedUserResponse);

        UserResponse result = userService.updateUser(authentication, updateRequest);

        assertEquals(updatedUserResponse, result);
        verify(authService, times(1)).encodePassword(newPassword);
        verify(userRepository, times(1)).save(existingUser);
    }
}
