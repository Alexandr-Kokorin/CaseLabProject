package caselab.controller.voting_process.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record VoteUserResponse(
    @Schema(description = "ID пользователя", example = "1")
    Long id,
    @Schema(description = "Email пользователя", example = "admin@mail.ru")
    String email,
    @Schema(description = "Имя пользователя", example = "Александр")
    String displayName
) {
}
