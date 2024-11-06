package caselab.controller.voting_process.payload;

import caselab.domain.entity.enums.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Ответ, содержащий информацию о голосе")
public record VoteResponse(
    @Schema(description = "Статус", example = "IN_FAVOUR")
    VoteStatus status,
    @Schema(description = "Пользователь")
    VoteUserResponse applicationUser
) {
}
