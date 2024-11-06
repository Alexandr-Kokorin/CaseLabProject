package caselab.controller.document.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Отывет, содержащий информацию о разрешении на работу с документом")
public record UserToDocumentResponse(
    @JsonProperty("email")
    @Schema(description = "Email пользователя", example = "user@example.com")
    String email,
    @JsonProperty("document_permissions")
    @ArraySchema(schema = @Schema(implementation = DocumentPermissionResponse.class,
                                  description = "Права доступа к документу"))
    List<DocumentPermissionResponse> documentPermissions
) {
}
