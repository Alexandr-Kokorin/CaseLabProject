package caselab.controller.attribute.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AttributeRequest(
    @JsonProperty("name")
    @NotBlank(message = "${attributes.request.name.is_blank}")
    @Size(min = 3, max = 25, message = "${attributes.request.name.invalid_size}")
    @Schema(description = "Имя атрибута", example = "Дата создания")
    String name,
    @JsonProperty("type")
    @NotBlank(message = "${attributes.request.type.is_blank}")
    @Schema(description = "Тип атрибута", example = "Дата")
    String type
) {
}
