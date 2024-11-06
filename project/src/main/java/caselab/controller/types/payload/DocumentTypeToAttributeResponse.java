package caselab.controller.types.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentTypeToAttributeResponse(
    @JsonProperty("attribute_id")
    @Schema(description = "ID аттрибута", example = "1")
    Long attributeId,
    @JsonProperty("is_optional")
    @Schema(description = "Признак обязательности аттрибута", example = "true")
    Boolean isOptional) {
}
