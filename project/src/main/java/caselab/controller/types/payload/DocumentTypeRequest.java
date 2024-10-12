package caselab.controller.types.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record DocumentTypeRequest(
    @JsonProperty("name")
    @NotBlank(message = "{document.type.request.name.is_blank}")
    @Size(min = 3, max = 25, message = "{document.type.request.name.invalid_size}")
    @Schema(description = "Название типа документа", example = "Кадровый")
    String name
) {
}
