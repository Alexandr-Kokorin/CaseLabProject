package caselab.service.security;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.GlobalPermissionRepository;
import caselab.exception.UserExistsException;
import caselab.service.secutiry.AuthenticationService;
import caselab.service.secutiry.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    @Mock
    private GlobalPermissionRepository globalPermissionRepository;

    @Mock
    private ApplicationUserRepository appUserRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void registerUser_success() {
        RegisterRequest registerRequest = createRegisterRequest("test@mail.com",
            "Test User",
            "password123");

        when(globalPermissionRepository.findByName(GlobalPermissionName.USER)).thenReturn(mockGlobalPermission());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(jwtService.generateToken(any(ApplicationUser.class))).thenReturn("mocked-jwt-token");
        when(appUserRepository.save(any(ApplicationUser.class)))
            .thenReturn(null); // Ничего не возвращаем, просто сохраняем

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        verify(appUserRepository, times(1)).save(any(ApplicationUser.class));
        verify(jwtService, times(1)).generateToken(any(ApplicationUser.class));
    }

    @Test
    @DisplayName("Ошибка при повторной регистрации с тем же email")
    void registerUser_userExists_throwsException() {
        RegisterRequest registerRequest = createRegisterRequest("test@mail.com",
            "Test User",
            "password123");

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockApplicationUser()));

        assertThrows(UserExistsException.class, () -> authenticationService.register(registerRequest));

        verify(appUserRepository, times(1)).findByEmail(anyString());
        verify(appUserRepository, never()).save(any(ApplicationUser.class));
    }

    @Test
    @DisplayName("Успешная аутентификация пользователя")
    void authenticateUser_success() {
        AuthenticationRequest authRequest = createAuthenticationRequest("test@mail.com", "password123");

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockApplicationUser()));
        when(jwtService.generateToken(any(ApplicationUser.class))).thenReturn("mocked-jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(any(ApplicationUser.class));
    }

    @Test
    @DisplayName("Ошибка аутентификации - пользователь не найден")
    void authenticateUser_userNotFound_throwsException() {
        AuthenticationRequest authRequest = createAuthenticationRequest("test@mail.com", "password123");

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authRequest));

        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(appUserRepository, times(1)).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(ApplicationUser.class));
    }

    @Test
    @DisplayName("Проверка хеширования пароля")
    void encodePassword_success() {
        String rawPassword = "password123";
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        String hashedPassword = authenticationService.encodePassword(rawPassword);

        assertNotNull(hashedPassword);
        assertEquals("hashedPassword", hashedPassword);
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    // Вспомогательные методы для мокирования данных

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

    private ApplicationUser mockApplicationUser() {
        return ApplicationUser.builder()
            .email("test@mail.com")
            .displayName("Test User")
            .hashedPassword("hashedPassword")
            .build();
    }

    private GlobalPermission mockGlobalPermission() {
        GlobalPermission permission = new GlobalPermission();
        permission.setName(GlobalPermissionName.USER);
        return permission;
    }
}
