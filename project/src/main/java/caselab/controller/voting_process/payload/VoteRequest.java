package caselab.controller.voting_process.payload;

import caselab.domain.entity.enums.VoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий информацию о голосе")
public record VoteRequest(
    @Positive(message = "{vote.request.id.not_positive}")
    @Schema(description = "ID документа", example = "1")
    long documentId,
    @NotNull(message = "{vote.request.status.is_null}")
    @Schema(description = "Статус", example = "IN_FAVOUR")
    VoteStatus status
) {
}
