package caselab.controller.document.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class DocumentResponseDTO {
    @Schema(description = "ID документа", example = "1")
    private Long id;
    @Schema(description = "ID Типа документа")
    private Long documentTypeId;
    @ArraySchema(schema = @Schema(description = "Список ID пользователей", example = "1"))
    private List<Long> applicationUserIds;
    @ArraySchema(schema = @Schema(implementation = DocumentAttributeValueDTO.class))
    private List<DocumentAttributeValueDTO> attributeValues;
}
