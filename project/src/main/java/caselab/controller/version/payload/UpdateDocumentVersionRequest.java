package caselab.controller.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateDocumentVersionRequest(
    @JsonProperty("name")
    String name
    //String content - если у версии поменялось содержимое - не должна ли это быть новая версия?
    // Остальные параметры (значения аттрибутов, подписи, голосования) должны обновляться через соответствующие CRUD-ы
) {
}
