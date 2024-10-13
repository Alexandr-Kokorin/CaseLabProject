package caselab.controller.voting_process.payload;

import caselab.domain.entity.enums.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record VoteRequest(
    @Positive
    @Schema(description = "ID пользователя", example = "1")
    Long applicationUserId,
    @Positive
    @Schema(description = "ID голосования", example = "1")
    Long votingProcessId,
    @NotNull
    @Schema(description = "Решение", example = "IN_FAVOUR")
    VoteStatus status
) {
}
