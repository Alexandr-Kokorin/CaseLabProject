package caselab.controller.voting_process.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Ответ, содержащий информацию о пользователе")
public record VoteUserResponse(
    @Schema(description = "Email пользователя", example = "user@example.com")
    String email,
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    String displayName
) {
}
