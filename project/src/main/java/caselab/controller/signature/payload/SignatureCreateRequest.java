package caselab.controller.signature.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignatureCreateRequest(
    @Schema(description = "ID версии документа", example = "1")
    @NotNull(message = "{signature.document_version_id.is_null}")
    Long documentVersionId,

    @Schema(description = "Название подписи", example = "Подпись договора")
    @Size(min = 5, max = 20, message = "{signature.name.size_invalid}")
    @NotBlank(message = "{signature.name.is_blank}")
    String name,

    @Schema(description = "Адрес электронной почты пользователя", example = "user@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotBlank(message = "{user.email.is_blank}")
    String email
) {
}
