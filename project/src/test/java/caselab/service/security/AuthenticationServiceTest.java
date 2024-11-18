package caselab.service.security;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.GlobalPermissionRepository;
import caselab.exception.entity.already_exists.UserAlreadyExistsException;
import caselab.service.notification.email.EmailService;
import caselab.service.secutiry.AuthenticationService;
import caselab.service.secutiry.JwtService;
import caselab.service.secutiry.RefreshTokenService;
import caselab.service.util.UserUtilService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private UserUtilService userUtilService;
    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void registerUser_success() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@mail.com")
            .displayName("Test User")
            .password("password123")
            .build();

        when(globalPermissionRepository.findByName(GlobalPermissionName.USER)).thenReturn(mockGlobalPermission());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(appUserRepository.save(any(ApplicationUser.class)))
            .thenReturn(null); // Ничего не возвращаем, просто сохраняем

        authenticationService.register(registerRequest, any(Authentication.class));

        assertAll(
            () -> verify(appUserRepository).save(any(ApplicationUser.class))
        );
    }

    @Test
    @DisplayName("Ошибка при повторной регистрации с тем же email")
    void registerUser_userExists_throwsException() {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@mail.com")
            .displayName("Test User")
            .password("password123")
            .build();

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockApplicationUser()));

        assertAll(
            () -> assertThrows(
                UserAlreadyExistsException.class,
                () -> authenticationService.register(registerRequest, any(Authentication.class))
            ),
            () -> verify(appUserRepository).findByEmail(anyString()),
            () -> verify(appUserRepository, never()).save(any(ApplicationUser.class))
        );
    }

    @Test
    @DisplayName("Успешная аутентификация пользователя")
    void authenticateUser_success() {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
            .email("test@mail.com")
            .password("password123")
            .build();

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockApplicationUser()));
        when(jwtService.generateToken(any(ApplicationUser.class))).thenReturn("mocked-jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.accessToken()).isEqualTo("mocked-jwt-token"),
            () -> verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class)),
            () -> verify(jwtService).generateToken(any(ApplicationUser.class))
        );
    }

    @Test
    @DisplayName("Ошибка аутентификации - пользователь не найден")
    void authenticateUser_userNotFound_throwsException() {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
            .email("test@mail.com")
            .password("password123")
            .build();

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertAll(
            () -> assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authRequest)),
            () -> verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class)),
            () -> verify(appUserRepository).findByEmail(anyString()),
            () -> verify(jwtService, never()).generateToken(any(ApplicationUser.class))
        );
    }

    @Test
    @DisplayName("Проверка хеширования пароля")
    void encodePassword_success() {
        String rawPassword = "password123";
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        String hashedPassword = authenticationService.encodePassword(rawPassword);

        assertAll(
            () -> assertThat(hashedPassword).isNotNull(),
            () -> assertThat(hashedPassword).isEqualTo("hashedPassword"),
            () -> verify(passwordEncoder).encode(anyString())
        );
    }

    // Вспомогательные методы для мокирования данных

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
