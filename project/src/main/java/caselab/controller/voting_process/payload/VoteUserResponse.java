package caselab.controller.voting_process.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Ответ, содержащий информацию о пользователе")
public record VoteUserResponse(
    @Schema(description = "ID пользователя", example = "1")
    Long id,
    @Schema(description = "Email пользователя", example = "admin@mail.ru")
    String email,
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    String displayName
) {
}
