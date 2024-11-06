package caselab.controller.document.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record AttributeValueResponse(
    @Schema(description = "id атрибута", example = "1")
    @JsonProperty("id")
    Long attributeId,
    @Schema(description = "Имя атрибута", example = "Дата создания")
    @JsonProperty("name")
    String name,
    @Schema(description = "Тип атрибута", example = "Дата")
    @JsonProperty("type")
    String type,
    @Schema(description = "Значение атрибута", example = "120")
    @JsonProperty("value")
    String value
) {
}
