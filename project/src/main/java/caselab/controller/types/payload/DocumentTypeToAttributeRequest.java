package caselab.controller.types.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentTypeToAttributeRequest(
    @Schema(description = "ID аттрибута")
    Long attributeId,
    @Schema(description = "Признак обязательности аттрибута", example = "true")
    Boolean isOptional) {

}
