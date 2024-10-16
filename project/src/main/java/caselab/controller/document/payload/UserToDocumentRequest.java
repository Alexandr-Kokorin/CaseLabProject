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
public record UserToDocumentRequest(
    @JsonProperty("email")
    @NotBlank(message = "{user.to.document.request.email.is_blank}")
    @Schema(description = "Email пользователя", example = "1")
    String email,
    @JsonProperty("document_permissions")
    @NotNull(message = "{user.to.document.request.document_permissions.is_blank}")
    @NotEmpty(message = "{user.to.document.request.document_permissions.is_empty}")
    @ArraySchema(schema = @Schema(implementation = Long.class,
                                  description = "id права доступа к документу",
                                  example = "1"))
    List<Long> documentPermissionIds
) {
}
