package caselab.controller.document.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий информацию о новой версии документа")
public record CreateDocumentVersionRequest(
    @Schema(description = "id документа", example = "1")
    @JsonProperty("document_id")
    Long documentId,
    @Schema(description = "Название версии документа", example = "Приказ об отпуске с изменённым описанием")
    @JsonProperty("name")
    String name,
    @Schema(description = "Значения аттрибутов текущей версии документа")
    @JsonProperty("attributes")
    List<AttributeValuePair> attributes
) {
}
