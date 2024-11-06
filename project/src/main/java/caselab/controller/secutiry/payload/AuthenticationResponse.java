package caselab.controller.secutiry.payload;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ аутентификации, содержащий JWT токен")
public record AuthenticationResponse(
    @Schema(description = "JWT токен для аутентифицированного пользователя",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token
) {
}
