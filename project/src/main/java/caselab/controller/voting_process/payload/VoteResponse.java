package caselab.controller.voting_process.payload;

import caselab.domain.entity.enums.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record VoteResponse(
    @Schema(description = "ID голоса", example = "1")
    Long id,
    @Schema(description = "Решение", example = "IN_FAVOUR")
    VoteStatus status,
    @Schema(description = "Пользователь")
    VoteUserResponse applicationUser,
    @Schema(description = "ID голосования", example = "1")
    Long VotingProcessId
) {
}
