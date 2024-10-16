package caselab.controller.types.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentTypeToAttributeResponse(
    @Schema(description = "ID аттрибута", example = "1")
    Long attributeId,
    @Schema(description = "Признак обязательности аттрибута", example = "true")
    Boolean isOptional) {
}
