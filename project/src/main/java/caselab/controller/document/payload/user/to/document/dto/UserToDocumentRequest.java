package caselab.controller.document.payload.user.to.document.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record UserToDocumentRequest(
    @Schema(description = "Email пользователя", example = "1")
    String email,
    @ArraySchema(schema = @Schema(description = "ID прав доступа к документу", example = "1"))
    List<Long> documentPermissionIds
) {
}
