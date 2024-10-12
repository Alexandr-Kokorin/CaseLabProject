package caselab.controller.document.payload.document.dto;

import caselab.controller.document.payload.user.to.document.dto.UserToDocumentRequest;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record DocumentRequest(
    @Schema(description = "ID документа", example = "1")
    Long id,
    @Schema(description = "ID Типа документа")
    Long documentTypeId,
    @ArraySchema(schema = @Schema(implementation = UserToDocumentRequest.class))
    List<UserToDocumentRequest> usersPermissions,
    @Schema(description = "Имя документа", example = "Приказ об отпуске")
    String name
) {
}
