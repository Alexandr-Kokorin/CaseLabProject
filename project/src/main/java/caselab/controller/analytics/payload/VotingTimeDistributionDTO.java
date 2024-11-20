package caselab.controller.analytics.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record VotingTimeDistributionDTO(
    @Schema(description = "Временной интервал", example = "5+ часов")
    String timeRange,
    @Schema(description = "Количество голосований", example = "5")
    Long count
) {
}
