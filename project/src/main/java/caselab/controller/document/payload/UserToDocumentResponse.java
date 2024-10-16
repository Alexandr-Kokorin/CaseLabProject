package caselab.controller.document.payload;

import caselab.domain.entity.DocumentPermission;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record UserToDocumentResponse(
    @JsonProperty("email")
    @Schema(description = "Email пользователя", example = "1")
    String email,
    @JsonProperty("document_permissions")
    @ArraySchema(schema = @Schema(implementation = DocumentPermission.class, description = "Права доступа к документу"))
    List<DocumentPermission> documentPermissions
) {
}
