package caselab.controller.voting_process.payload;

import caselab.domain.entity.Vote;
import caselab.domain.entity.enums.VotingProcessStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;


@Builder
public record VotingProcessResponse(
    @Schema(description = "ID голосования", example = "1")
    Long id,
    @Schema(description = "Название", example = "???")
    String name,
    @Schema(description = "Порог принятия", example = "0.6")
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
