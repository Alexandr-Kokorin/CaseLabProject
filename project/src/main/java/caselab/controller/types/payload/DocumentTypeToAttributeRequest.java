package caselab.controller.types.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record DocumentTypeToAttributeRequest(
    @JsonProperty("attribute_id")
    @Positive(message = "{document.type.to.attributes.request.attributes.id.not.positive}")
    @NotNull(message = "{document.type.to.attributes.request.attributes.id.is_blank}")
    @Schema(description = "ID аттрибута", example = "1")
    Long attributeId,
    @JsonProperty("is_optional")
    @NotNull(message = "{document.type.to.attributes.request.is.optional.is_blank}")
    @Schema(description = "Признак обязательности аттрибута", example = "true")
    Boolean isOptional) {
}
