package caselab.controller.substitution.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на делегирование документа")
public record DelegationRequest(
    @Schema(description = "ID документа", example = "1")
    @Positive(message = "{voting.process.request.document_id.not_positive}")
    @JsonProperty("document_id")
    Long documentId,
    @Schema(description = "Адрес электронной почты делегата", example = "user@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotBlank(message = "{user.mail.is_blank}")
    @JsonProperty("delegate_email")
    String delegateEmail
) {
}
