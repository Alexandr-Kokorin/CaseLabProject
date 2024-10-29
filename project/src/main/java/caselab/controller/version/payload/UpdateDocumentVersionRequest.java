package caselab.controller.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление версии документа")
public record UpdateDocumentVersionRequest(
    @Schema(description = "Название версии документа", example = "Приказ об отпуске с изменённым описанием")
    @JsonProperty("name")
    String name
    //String content - если у версии поменялось содержимое - не должна ли это быть новая версия?
    // Остальные параметры (значения аттрибутов, подписи, голосования) должны обновляться через соответствующие CRUD-ы
) {
}
