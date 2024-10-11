package caselab.controller.document.payload.user.to.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserToDocumentResponse(
    @Schema(description = "ID пользователя", example = "1") Long id
) {
}
