package caselab.controller.voting_process.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий информацию о голосовании")
public record VotingProcessRequest(
    @NotBlank(message = "Название не может быть пустым")
    @Schema(description = "Название", example = "Голосование")
    String name,
    @NotNull(message = "Порог принятия не может быть null")
    @Positive(message = "Порог принятия должен быть больше 0")
    @Schema(description = "Порог принятия", example = "0.6")
    Double threshold,
    @NotNull(message = "Время до дедлайна не может быть null")
    @Positive(message = "Время до дедлайна должно быть больше 0")
    @Schema(description = "Время до дедлайна (в днях)", example = "7")
    Long deadline,
    @NotNull(message = "ID версии документа не может быть null")
    @Positive(message = "ID версии документа должно быть больше 0")
    @Schema(description = "ID версии документа", example = "1")
    Long documentVersionId,
    @NotEmpty(message = "Массив пользователей не может быть пустым")
    @ArraySchema(schema = @Schema(implementation = Long.class, description = "ID пользователя", example = "1"))
    List<Long> userIds
) {
}
