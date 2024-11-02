package caselab.controller.document.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Пары ключ-значение между атрибутом и его значением")
public record AttributeValuePair(
    @Schema(description = "id атрибута", example = "1")
    @JsonProperty("attributeId")
    Long attributeId,
    @Schema(description = "Значение атрибута", example = "120")
    @JsonProperty("value")
    String value
) {
}
