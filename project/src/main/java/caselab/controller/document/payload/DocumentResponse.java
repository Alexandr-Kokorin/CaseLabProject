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
    @Schema(description = "Имя документа", example = "Приказ об отпуске")
    String name,
    @ArraySchema(schema = @Schema(implementation = Long.class))
    List<Long> documentVersionIds,
    @ArraySchema(schema = @Schema(implementation = UserToDocumentResponse.class))
    List<UserToDocumentResponse> usersPermissions
) {
}
