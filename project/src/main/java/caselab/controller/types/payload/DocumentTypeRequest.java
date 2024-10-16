package caselab.controller.types.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на создание/обновление типа документа, содержащий информацию о типе документа")
public record DocumentTypeRequest(
    @JsonProperty("name")
    @NotBlank(message = "{document.type.request.name.is_blank}")
    @Size(min = 3, max = 25, message = "{document.type.request.name.invalid_size}")
    @Schema(description = "Название типа документа", example = "Кадровый")
    String name,
    @JsonProperty("attributes")
    @ArraySchema(schema = @Schema(description = "Список аттрибутов для создаваемого типа документа",
                                  implementation = DocumentTypeToAttributeRequest.class))
    @NotNull(message = "{document.type.request.attributes.is.blank}")
    @NotEmpty(message = "{document.type.request.attributes.is.empty}")
    List<@Valid DocumentTypeToAttributeRequest> attributeRequests
) {
}
