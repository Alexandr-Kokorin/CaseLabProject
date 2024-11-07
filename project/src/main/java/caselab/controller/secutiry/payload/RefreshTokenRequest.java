package caselab.controller.secutiry.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на обновление jwt токена пользователя")
public record RefreshTokenRequest(
    @Schema(description = "старый токен пользователя",
            example = "d33fcba4-b8cf-44db-8641-b299421b3c26")
    @Size(min = 36, max = 36)
    @NotBlank(message = "user.token.is_blank")
    String token
) {
}
