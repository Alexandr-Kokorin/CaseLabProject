package caselab.controller.document.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record DocumentResponse(
    @Schema(description = "ID документа", example = "1")
    Long id,

    @Schema(description = "ID Типа документа")
    Long documentTypeId,

    @ArraySchema(schema = @Schema(description = "Список ID пользователей", example = "1"))
    List<Long> applicationUserIds,

    @ArraySchema(schema = @Schema(implementation = DocumentAttributeValueDTO.class))
    List<DocumentAttributeValueDTO> attributeValues
) {}
