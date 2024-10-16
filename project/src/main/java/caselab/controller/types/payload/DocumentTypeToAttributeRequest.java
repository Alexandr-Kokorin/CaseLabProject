package caselab.controller.types.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record DocumentTypeToAttributeRequest(
    @Positive
    @NotNull
    @Schema(description = "ID аттрибута")
    Long attributeId,
    @Schema(description = "Признак обязательности аттрибута", example = "true")
    Boolean isOptional
) {
}
