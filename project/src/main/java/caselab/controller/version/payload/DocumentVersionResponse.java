package caselab.controller.version.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(description = "Ответ, содержащий информацию о версии документа")
public class DocumentVersionResponse {
    @Schema(description = "id версии документа", example = "3")
    Long id;
    @Schema(description = "Значения аттрибутов текущей версии документа")
    List<AttributeValuePair> attributes;
    @Schema(description = "Название версии документа", example = "Приказ об отпуске с изменённым описанием")
    String name;
    @Schema(description = "Дата создания текущей версии документа", example = "")
    OffsetDateTime createdAt;
    @Schema(description = "id документа", example = "1")
    Long documentId;
    @Schema(description = "id подписей к текущей версии документа")
    List<Long> signatureIds;
    @Schema(description = "id голосов к текущей версии документа")
    List<Long> votingProcessesId;
    @Schema(description = "Адрес расположения файла версии документа в хранилище")
    String contentName;
}
