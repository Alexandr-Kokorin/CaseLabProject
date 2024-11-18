package caselab.service.secutiry;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RefreshTokenRequest;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.RefreshToken;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.GlobalPermissionRepository;
import caselab.exception.entity.already_exists.UserAlreadyExistsException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@SuppressWarnings("LineLength")
@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserUtilService userUtilService;

    private final GlobalPermissionRepository globalPermissionRepository;
    private final ApplicationUserRepository appUserRepository;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    public void register(RegisterRequest request, Authentication authentication) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        checkEmail(request.email());

        var globalPermission = globalPermissionRepository.findByName(GlobalPermissionName.USER);
        var user = ApplicationUser.builder()
            .email(request.email())
            .displayName(request.displayName())
            .globalPermissions(List.of(globalPermission))
            .hashedPassword(encodePassword(request.password()))
            .build();
        appUserRepository.save(user);
        sendMessage(request);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            ));

        var user = appUserRepository.findByEmail(request.email())
            .orElseThrow();
        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.create(user.getEmail());

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenService.getByToken(request.token());

        refreshTokenService.verifyExpiration(token);

        var user = appUserRepository.findByEmail(token.getApplicationUser().getEmail()).orElseThrow(
            () -> new UserNotFoundException(token.getApplicationUser().getEmail())
        );

        refreshTokenService.delete(token);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.create(user.getEmail());

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void checkEmail(String email) {
        var applicationUser = appUserRepository.findByEmail(email);
        if (applicationUser.isPresent()) {
            throw new UserAlreadyExistsException(email);
        }
    }

    private void sendMessage(RegisterRequest request) {
        var emailDetails = EmailNotificationDetails.builder()
            .sender("admin@solifex.ru")
            .recipient(request.email())
            .subject("Уведомление о регистрации")
            .text("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Добро пожаловать</title>
                </head>
                <body style="font-family: Arial, sans-serif; color: #333;">
                  <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border: 1px solid #ddd;">
                    <h2 style="color: #0078cf;">Здравствуйте, %s!</h2>
                    <p>Вы успешно зарегистрированы на сайте <strong>[сайт]</strong>.</p>
                    <p><strong>Ваш email:</strong> %s</p>
                    <p><strong>Пароль:</strong> %s</p>
                    <p>Для связи с администратором нажмите <a href="[ссылка]" style="color: #0078cf;">здесь</a>.</p>
                  </div>
                </body>
                </html>
                """.formatted(request.displayName(), request.email(), request.password()))
            .attachments(List.of())
            .build();

        emailService.sendNotification(emailDetails);
    }
}
