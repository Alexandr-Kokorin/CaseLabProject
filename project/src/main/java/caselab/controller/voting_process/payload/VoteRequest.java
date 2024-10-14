package caselab.controller.voting_process.payload;

import caselab.domain.entity.enums.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий информацию о голосе")
public record VoteRequest(
    @NotNull(message = "ID пользователя не может быть null")
    @Positive(message = "ID пользователя должно быть больше 0")
    @Schema(description = "ID пользователя", example = "1")
    Long applicationUserId,
    @NotNull(message = "ID голосования не может быть null")
    @Positive(message = "ID голосования должно быть больше 0")
    @Schema(description = "ID голосования", example = "1")
    Long votingProcessId,
    @NotNull(message = "Статус не может быть null")
    @Schema(description = "Статус", example = "IN_FAVOUR")
    VoteStatus status
) {
}
