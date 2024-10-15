package caselab.controller.document.payload.user.to.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserToDocumentResponse(
    @Schema(description = "Email пользователя", example = "1")
    String email
) {
}
