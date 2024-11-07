package caselab.controller.secutiry.payload;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ аутентификации, содержащий JWT токен")
public record AuthenticationResponse(
    @Schema(description = "JWT токен для аутентифицированного пользователя",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    @Schema(description = "Токен для обновления на новый JWT токен",
            example = "d33fcba4-b8cf-44db-8641-b299421b3c26")
    String refreshToken
) {
}
