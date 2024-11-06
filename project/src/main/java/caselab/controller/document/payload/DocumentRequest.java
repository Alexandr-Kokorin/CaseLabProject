package caselab.controller.document.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий информацию о документе")
public record DocumentRequest(
    @JsonProperty("document_type_id")
    @NotNull(message = "{document.request.document.type.id.is_blank}")
    @Schema(description = "id типа документа", example = "1")
    Long documentTypeId,
    @JsonProperty("name")
    @NotBlank(message = "{document.request.name.is_blank}")
    @Schema(description = "Имя документа", example = "Приказ об отпуске")
    String name,
    @JsonProperty("users_permissions")
    @NotNull(message = "{document.request.users.permissions.is_blank}")
    @NotEmpty(message = "{document.request.users.permissions.is_empty}")
    @ArraySchema(schema = @Schema(implementation = UserToDocumentRequest.class,
                                  description = "Список разрешений пользователей на работу с документом"))
    List<UserToDocumentRequest> usersPermissions
) {
}
