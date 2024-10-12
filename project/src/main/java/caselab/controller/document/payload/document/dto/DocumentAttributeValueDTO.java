package caselab.controller.document.payload.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentAttributeValueDTO(
    @Schema(description = "ID атрибута", example = "1")
    Long id,
    @Schema(description = "Значение атрибута, должно соответствовать типу атрибута", example = "Some value")
    String value
) {
}
