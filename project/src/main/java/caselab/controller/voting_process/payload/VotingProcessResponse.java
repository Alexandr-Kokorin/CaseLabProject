package caselab.controller.voting_process.payload;

import caselab.domain.entity.Vote;
import caselab.domain.entity.enums.VotingProcessStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record VotingProcessResponse(
    @Schema(description = "ID голосования", example = "1")
    Long id,
    @Schema(description = "Название", example = "???")
    String name,
    @Schema(description = "Порог принятия (в процентах)", example = "60")
    Double threshold,
    @Schema(description = "Результат голосования", example = "ACCEPTED")
    VotingProcessStatus status,
    @Schema(description = "Время создания", example = "2023-10-31T01:30+01:00")
    OffsetDateTime createdAt,
    @Schema(description = "Время окончания", example = "2023-10-31T01:30+01:00")
    OffsetDateTime deadline,
    @Schema(description = "ID версии документа", example = "1")
    Long documentVersionId,
    @ArraySchema(schema = @Schema(implementation = Vote.class, description = "Голос"))
    List<VoteResponse> votes
) {
}
