package caselab.controller.analytics.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record SystemLoadByHourDTO(
    @Schema(description = "Начало интервала", example = "01:00:00")
    LocalTime startTime,
    @Schema(description = "Конец интервала", example = "02:00:00")
    LocalTime endTime,
    @Schema(description = "Количество изменений", example = "30")
    Long count
) {
}
