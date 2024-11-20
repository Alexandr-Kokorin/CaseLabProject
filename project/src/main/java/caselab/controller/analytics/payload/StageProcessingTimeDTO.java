package caselab.controller.analytics.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record StageProcessingTimeDTO(
    @Schema(description = "Название состояния", example = "Отправка")
    String stage,
    @Schema(description = "Среднее время", example = "30")
    Double averageTimeInMinutes
) {
}
