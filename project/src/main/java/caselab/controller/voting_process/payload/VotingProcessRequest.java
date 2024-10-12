package caselab.controller.voting_process.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import java.util.List;

@Builder
public record VotingProcessRequest(
    @NotBlank
    @Schema(description = "Название", example = "1")
    String name,
    @Positive
    @Schema(description = "Порог принятия", example = "0.6")
    Double threshold,
    @Positive
    @Schema(description = "Время до дедлайна (в днях)", example = "7")
    Long deadline,
    @Positive
    @Schema(description = "ID версии документа", example = "1")
    Long documentVersionId,
    @NotEmpty
    @ArraySchema(schema = @Schema(implementation = Long.class, description = "ID пользователя", example = "1"))
    List<Long> userIds
) {
}
