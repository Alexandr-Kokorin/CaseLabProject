package caselab.controller.types.payload;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentTypeRequest(
    @Schema(description = "Название типа документа", example = "Кадровый")
    String name
) {
}
