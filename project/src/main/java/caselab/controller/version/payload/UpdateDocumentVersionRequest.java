package caselab.controller.version.payload;

public record UpdateDocumentVersionRequest(
    String name
  //String content - если у версии поменялось содержимое - не должна ли это быть новая версия?
    // Остальные параметры (значения аттрибутов, подписи, голосования) должны обновляться через соответствующие CRUD-ы
) {
}
