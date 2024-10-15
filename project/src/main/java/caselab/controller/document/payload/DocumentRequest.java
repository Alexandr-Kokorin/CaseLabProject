package caselab.controller.document.payload;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record DocumentRequest(
    @Schema(description = "ID Типа документа")
    Long documentTypeId,
    @Schema(description = "Имя документа", example = "Приказ об отпуске")
    String name,
    @ArraySchema(schema = @Schema(implementation = UserToDocumentRequest.class))
    List<UserToDocumentRequest> usersPermissions
) {
}
