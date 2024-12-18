package caselab.controller.voting_process.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий информацию о голосовании")
public record VotingProcessRequest(
    @NotBlank(message = "{voting.process.request.name.is_blank}")
    @Schema(description = "Название", example = "Голосование")
    String name,
    @Positive(message = "{voting.process.request.threshold.not_positive}")
    @Schema(description = "Порог принятия", example = "0.6")
    double threshold,
    @NotNull(message = "{voting.process.request.deadline.is_null}")
    @Schema(description = "Время до дедлайна", example = "P1DT2H")
    Duration deadline,
    @Positive(message = "{voting.process.request.document_id.not_positive}")
    @Schema(description = "ID документа", example = "1")
    long documentId,
    @NotEmpty(message = "{voting.process.request.emails.is_empty}")
    @ArraySchema(schema = @Schema(implementation = String.class,
                                  description = "Email пользователя",
                                  example = "user@example.com"))
    List<String> emails
) {
}
