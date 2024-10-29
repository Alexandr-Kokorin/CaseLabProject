package caselab.controller.document.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Ответ, содержащий информацию о документе")
public record DocumentResponse(
    @JsonProperty("id")
    @Schema(description = "ID документа", example = "1")
    Long id,
    @JsonProperty("document_type_id")
    @Schema(description = "ID типа документа", example = "1")
    Long documentTypeId,
    @JsonProperty("name")
    @Schema(description = "Имя документа", example = "Приказ об отпуске")
    String name,
    @JsonProperty("document_versions_ids")
    @ArraySchema(schema = @Schema(implementation = Long.class, description = "Список id версий документов"))
    List<Long> documentVersionIds,
    @JsonProperty("user_permissions")
    @ArraySchema(schema = @Schema(implementation = UserToDocumentResponse.class,
                                  description = "Список разрешений пользователей, имеющих доступ к документу"))
    List<UserToDocumentResponse> usersPermissions,
    @JsonProperty("status")
    String status
) {
}
