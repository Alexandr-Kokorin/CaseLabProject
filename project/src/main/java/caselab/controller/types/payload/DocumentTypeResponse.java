package caselab.controller.types.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record DocumentTypeResponse(
    @Schema(description = "ID типа документа", example = "1")
    Long id,
    @Schema(description = "Название типа документа", example = "Кадровый")
    String name,
    @ArraySchema(schema = @Schema(implementation = DocumentTypeToAttributeResponse.class))
    List<DocumentTypeToAttributeResponse> attributeResponses
){
}
