package caselab.controller.secutiry;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RefreshTokenRequest;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.service.secutiry.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API управления аутентификацией")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация нового пользователя",
               description = "Регистрирует нового пользователя с предоставленной информацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Пользователь уже существует",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/register")
    public void register(Authentication authentication, @Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request, authentication);
    }

    @Operation(summary = "Аутентификация пользователя",
               description = "Аутентифицирует пользователя с предоставленными учетными данными")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                     content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request);
    }
}
