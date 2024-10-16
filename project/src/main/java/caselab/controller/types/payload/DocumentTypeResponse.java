package caselab.controller.types.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Ответ создания/обновления типа документа")
public record DocumentTypeResponse(
    @Schema(description = "ID типа документа", example = "1")
    Long id,
    @Schema(description = "Название типа документа", example = "Кадровый")
    String name,
    @JsonProperty("attributes")
    @ArraySchema(schema = @Schema(description = "Список аттрибутов типа документа",
                                  implementation = DocumentTypeToAttributeResponse.class))
    List<DocumentTypeToAttributeResponse> attributeResponses
){
}
