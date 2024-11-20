package caselab.controller.analytics.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentTypeDistributionDTO(
    @Schema(description = "Название типа", example = "Договор")
    String typeName,
    @Schema(description = "Количество документов этого типа", example = "12")
    Long count
) {
}
